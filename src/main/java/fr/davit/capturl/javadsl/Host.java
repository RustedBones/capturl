package fr.davit.capturl.javadsl;

import fr.davit.capturl.scaladsl.Host$;

public abstract class Host {

    public static Host create(String address) {
        return Host$.MODULE$.apply(address);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Host
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isEmpty();

    public abstract String getAddress();

    public abstract boolean isIPv4();

    public abstract boolean isIPv6();
    public abstract boolean isNamedHost();

    public abstract fr.davit.capturl.scaladsl.Host asScala();
}
