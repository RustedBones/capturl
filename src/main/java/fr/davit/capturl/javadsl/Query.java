package fr.davit.capturl.javadsl;

import fr.davit.capturl.scaladsl.Query$;

public abstract class Query {

    public static Query create(String query) { return Query$.MODULE$.apply(query); }

    //------------------------------------------------------------------------------------------------------------------
    // Query
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isEmpty();

    public abstract Iterable<QueryParameter> getParameters();

    public abstract fr.davit.capturl.scaladsl.Query asScala();
}
