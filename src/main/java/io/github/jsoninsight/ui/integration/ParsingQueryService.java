package io.github.jsoninsight.ui.integration;

import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.QueryResult;
import io.github.jsoninsight.query.lexer.QueryLexer;
import io.github.jsoninsight.query.lexer.QueryLexerException;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.QueryParser;
import io.github.jsoninsight.query.parser.QueryParserException;
import io.github.jsoninsight.query.parser.impl.QueryParserImpl;
import io.github.jsoninsight.service.QueryService;

import java.util.List;
import java.util.Optional;

/**
 * Cienki adapter integracyjny: walidację zapytań robi przez prawdziwy lekser i parser
 * Filipa, ale wykonanie deleguje do dostarczonego (na razie stubowego) QueryService.
 *
 * Gdy pojawi się prawdziwy evaluator AST, wystarczy podmienić delegata w MainController.
 */
public class ParsingQueryService implements QueryService {

    private final QueryLexer lexer = new QueryLexerImpl();
    private final QueryParser parser = new QueryParserImpl();
    private final QueryService executionDelegate;

    public ParsingQueryService(QueryService executionDelegate) {
        this.executionDelegate = executionDelegate;
    }

    @Override
    public Optional<String> validateQuery(String query) {
        if (query == null || query.isBlank()) {
            return Optional.of("Zapytanie jest puste.");
        }
        try {
            List<QueryToken> tokens = lexer.tokenize(query);
            parser.parse(tokens);
            return Optional.empty();
        } catch (QueryLexerException e) {
            return Optional.of("Lexer: " + e.getMessage());
        } catch (QueryParserException e) {
            return Optional.of("Parser: " + e.getMessage()
                    + " (pozycja " + e.getPosition() + ")");
        } catch (RuntimeException e) {
            return Optional.of(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    @Override
    public QueryResult executeQuery(String query, List<JsonDocument> documents) {
        return executionDelegate.executeQuery(query, documents);
    }
}
