package fr.davit.capturl.javadsl;

import java.util.Map;

public abstract class Query {

    public abstract boolean isEmpty();

    public abstract Iterable<QueryParameter> getParameters();
}
