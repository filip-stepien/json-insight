package io.github.jsoninsight.json;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SchemaGenerator {

    public static String generateSchema(String jsonContent) {
        JsonLexer jsonLexer = new JsonLexer(jsonContent);
        List<Token> tokens = jsonLexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        JsonNode root = parser.parse();
        return nodeToString(inferSchema(root));
    }

    public static boolean schemasMatch(String schema1, String schema2) {
        String s1 = generateSchema(schema1);
        String s2 = generateSchema(schema2);
        JsonNode tree1 = parseToNode(s1);
        JsonNode tree2 = parseToNode(s2);
        return nodesEqual(tree1, tree2);
    }

    private static JsonNode parseToNode(String json) {
        JsonLexer jsonLexer = new JsonLexer(json);
        List<Token> tokens = jsonLexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        return parser.parse();
    }

    private static JsonNode inferSchema(JsonNode node) {
        return switch (node) {
            case JsonNode.StringNode ignored ->
                    object("type", string("string"));
            case JsonNode.BooleanNode ignored ->
                    object("type", string("boolean"));
            case JsonNode.NullNode ignored ->
                    object("type", string("null"));
            case JsonNode.NumberNode n ->
                    object("type", string(n.isFloat() ? "number" : "integer"));
            case JsonNode.ArrayNode a ->
                    inferArraySchema(a);
            case JsonNode.ObjectNode o ->
                    inferObjectSchema(o);
        };
    }

    private static JsonNode inferArraySchema(JsonNode.ArrayNode arr) {
        java.util.LinkedHashMap<String, JsonNode> fields = new java.util.LinkedHashMap<>();
        fields.put("type", string("array"));

        if (!arr.elements().isEmpty()) {
            JsonNode itemSchema = inferSchema(arr.elements().get(0));
            fields.put("items", itemSchema);
        }

        return new JsonNode.ObjectNode(fields);
    }

    private static JsonNode inferObjectSchema(JsonNode.ObjectNode obj) {
        java.util.LinkedHashMap<String, JsonNode> fields = new java.util.LinkedHashMap<>();
        fields.put("type", string("object"));

        java.util.LinkedHashMap<String, JsonNode> properties = new java.util.LinkedHashMap<>();
        java.util.ArrayList<String> requiredNames = new java.util.ArrayList<>();

        for (Map.Entry<String, JsonNode> entry : obj.fields().entrySet()) {
            properties.put(entry.getKey(), inferSchema(entry.getValue()));
            requiredNames.add(entry.getKey());
        }

        java.util.Collections.sort(requiredNames);
        java.util.ArrayList<JsonNode> required = new java.util.ArrayList<>();
        for (String name : requiredNames) {
            required.add(string(name));
        }

        fields.put("properties", new JsonNode.ObjectNode(properties));
        fields.put("required", new JsonNode.ArrayNode(required));

        return new JsonNode.ObjectNode(fields);
    }

    private static JsonNode.ObjectNode object(String key, JsonNode value) {
        java.util.LinkedHashMap<String, JsonNode> map = new java.util.LinkedHashMap<>();
        map.put(key, value);
        return new JsonNode.ObjectNode(map);
    }

    private static JsonNode.StringNode string(String value) {
        return new JsonNode.StringNode(value);
    }

    static String nodeToString(JsonNode node) {
        StringBuilder sb = new StringBuilder();
        appendNode(sb, node);
        return sb.toString();
    }

    private static void appendNode(StringBuilder sb, JsonNode node) {
        switch (node) {
            case JsonNode.StringNode s -> {
                sb.append('"');
                escapeJson(sb, s.value());
                sb.append('"');
            }
            case JsonNode.NumberNode n -> sb.append(n.raw());
            case JsonNode.BooleanNode b -> sb.append(b.value());
            case JsonNode.NullNode ignored -> sb.append("null");
            case JsonNode.ArrayNode a -> {
                sb.append('[');
                Iterator<JsonNode> it = a.elements().iterator();
                while (it.hasNext()) {
                    appendNode(sb, it.next());
                    if (it.hasNext()) sb.append(',');
                }
                sb.append(']');
            }
            case JsonNode.ObjectNode o -> {
                sb.append('{');
                Iterator<Map.Entry<String, JsonNode>> it = o.fields().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> e = it.next();
                    sb.append('"');
                    escapeJson(sb, e.getKey());
                    sb.append('"');
                    sb.append(':');
                    appendNode(sb, e.getValue());
                    if (it.hasNext()) sb.append(',');
                }
                sb.append('}');
            }
        }
    }

    static void escapeJson(StringBuilder sb, String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append("\\u");
                        String hex = Integer.toHexString(c);
                        for (int j = hex.length(); j < 4; j++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
    }

    static boolean nodesEqual(JsonNode a, JsonNode b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        return switch (a) {
            case JsonNode.StringNode sa when b instanceof JsonNode.StringNode sb ->
                    sa.value().equals(sb.value());
            case JsonNode.NumberNode na when b instanceof JsonNode.NumberNode nb ->
                    na.raw().equals(nb.raw()) && na.isFloat() == nb.isFloat();
            case JsonNode.BooleanNode ba when b instanceof JsonNode.BooleanNode bb ->
                    ba.value() == bb.value();
            case JsonNode.NullNode ignored when b instanceof JsonNode.NullNode ->
                    true;
            case JsonNode.ArrayNode aa when b instanceof JsonNode.ArrayNode ab ->
                    listsEqual(aa.elements(), ab.elements());
            case JsonNode.ObjectNode oa when b instanceof JsonNode.ObjectNode ob ->
                    objectsEqual(oa.fields(), ob.fields());
            default -> false;
        };
    }

    private static boolean listsEqual(List<JsonNode> a, List<JsonNode> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!nodesEqual(a.get(i), b.get(i))) return false;
        }
        return true;
    }

    private static boolean objectsEqual(Map<String, JsonNode> a, Map<String, JsonNode> b) {
        if (a.size() != b.size()) return false;
        for (Map.Entry<String, JsonNode> entry : a.entrySet()) {
            JsonNode bVal = b.get(entry.getKey());
            if (bVal == null || !nodesEqual(entry.getValue(), bVal)) return false;
        }
        return true;
    }
}
