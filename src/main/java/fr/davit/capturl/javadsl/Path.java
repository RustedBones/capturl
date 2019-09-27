package fr.davit.capturl.javadsl;

import fr.davit.capturl.scaladsl.Path$;

public abstract class Path {

    public static Path create(String path) { return Path$.MODULE$.apply(path); }

    //------------------------------------------------------------------------------------------------------------------
    // Path
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isEmpty();

    public abstract boolean isAbsolute();

    public abstract boolean isRelative();

    public abstract boolean isResource();

    public abstract boolean isDirectory();

    public abstract Path relativize(Path path);

    public abstract Path resolve(Path path);

    public abstract Path normalize();

    public abstract Path appendSlash();

    public abstract Path appendSegment(String segment);

    public abstract Iterable<String> getSegments();

    public abstract fr.davit.capturl.scaladsl.Path asScala();

}
