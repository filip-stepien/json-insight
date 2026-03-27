package io.github.jsoninsight.model;

import java.util.List;

public class QueryResult {

    private List<JsonDocument> matchedDocuments;
    private String executedQuery;
    private int totalMatches;

    public QueryResult() {
    }

    public QueryResult(List<JsonDocument> matchedDocuments, String executedQuery) {
        this.matchedDocuments = matchedDocuments;
        this.executedQuery = executedQuery;
        this.totalMatches = matchedDocuments.size();
    }

    public List<JsonDocument> getMatchedDocuments() {
        return matchedDocuments;
    }

    public void setMatchedDocuments(List<JsonDocument> matchedDocuments) {
        this.matchedDocuments = matchedDocuments;
        this.totalMatches = matchedDocuments.size();
    }

    public String getExecutedQuery() {
        return executedQuery;
    }

    public void setExecutedQuery(String executedQuery) {
        this.executedQuery = executedQuery;
    }

    public int getTotalMatches() {
        return totalMatches;
    }
}
