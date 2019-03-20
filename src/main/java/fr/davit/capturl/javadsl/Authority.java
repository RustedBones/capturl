package fr.davit.capturl.javadsl;

import java.util.Optional;

public abstract class Authority {

    public abstract boolean isEmpty();

    public abstract Host getHost();

    public abstract Optional<Integer> getPort();

    public abstract Optional<String> getUserInfo();

    public static Authority create(String authority) {
        return fr.davit.capturl.scaladsl.Authority$.MODULE$.apply(authority);
    }
}
