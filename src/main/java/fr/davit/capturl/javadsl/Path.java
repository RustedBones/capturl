package fr.davit.capturl.javadsl;

public abstract class Path {

    public abstract boolean isEmpty();

    public abstract boolean isAbsolute();

    public abstract boolean isRelative();

    public abstract Iterable<String> getSegments();

}
