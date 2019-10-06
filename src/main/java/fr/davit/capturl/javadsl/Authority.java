package fr.davit.capturl.javadsl;

import fr.davit.capturl.scaladsl.Authority$;

import java.util.Optional;

public abstract class Authority {

    public static Authority create(String authority) { return Authority$.MODULE$.apply(authority); }

    //------------------------------------------------------------------------------------------------------------------
    // Authority
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isEmpty();

    public abstract fr.davit.capturl.scaladsl.Authority asScala();

    //------------------------------------------------------------------------------------------------------------------
    // Host
    //------------------------------------------------------------------------------------------------------------------
    public abstract Host getHost();

    public abstract Authority withHost(String host);

    //------------------------------------------------------------------------------------------------------------------
    // Port
    //------------------------------------------------------------------------------------------------------------------
    public abstract Optional<Integer> getPort();

    public abstract Authority withPort(int port);

    public abstract Optional<String> getUserInfo();

    public abstract Authority withUserInfo(String userInfo);
}
