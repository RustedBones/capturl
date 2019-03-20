package fr.davit.capturl.javadsl;

import java.util.Optional;

public abstract class Iri {

    public abstract String getScheme();

    public abstract Authority getAuthority();

    public abstract Path getPath();

    public abstract Query getQuery();

    public abstract Optional<String> getFragment();

    public abstract boolean isEmpty();

    public abstract boolean isAbsolute();

    public abstract boolean isRelative();

}
