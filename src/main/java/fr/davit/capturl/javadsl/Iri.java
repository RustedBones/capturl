package fr.davit.capturl.javadsl;

import fr.davit.capturl.scaladsl.Iri$;

public abstract class Iri {

    public static Iri create(String iri) { return Iri$.MODULE$.apply(iri); }

    public static Iri resolve(Iri base, Iri iri) { return null; } // TODO

    public static final Iri EMPTY = fr.davit.capturl.scaladsl.Iri.EMPTY;

    //------------------------------------------------------------------------------------------------------------------
    // Iri
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isAbsolute();

    public abstract boolean isRelative();

    public abstract Iri relativize(Iri iri);

    public abstract Iri resolve(Iri iri);

    public abstract fr.davit.capturl.scaladsl.Iri asScala();


    //------------------------------------------------------------------------------------------------------------------
    // Scheme
    //------------------------------------------------------------------------------------------------------------------
    public abstract String getScheme();

    public abstract Iri withScheme(String scheme);

    //------------------------------------------------------------------------------------------------------------------
    // Authority
    //------------------------------------------------------------------------------------------------------------------
    public abstract Authority getAuthority();

    public abstract Iri withAuthority(Authority authority);

    public abstract Iri withAuthority(String authority);

    //------------------------------------------------------------------------------------------------------------------
    // Path
    //------------------------------------------------------------------------------------------------------------------
    public abstract Path getPath();

    public abstract Iri withPath(Path path);

    public abstract Iri withPath(String path);


    //------------------------------------------------------------------------------------------------------------------
    // Query
    //------------------------------------------------------------------------------------------------------------------
    public abstract Query getQuery();

    public abstract Iri withQuery(Query query);

    public abstract Iri withQuery(String query);

    //------------------------------------------------------------------------------------------------------------------
    // Fragment
    //------------------------------------------------------------------------------------------------------------------
    public abstract String getFragment();

    public abstract Iri withFragment(String fragment);

}
