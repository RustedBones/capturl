package fr.davit.capturl.javadsl;

import java.util.Optional;

public class QueryParameter {

    private String key;
    private String value;

    public QueryParameter(String key) {
        this(key, null);
    }

    public QueryParameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Optional<String> getValue() {
        return Optional.of(value);
    }
}