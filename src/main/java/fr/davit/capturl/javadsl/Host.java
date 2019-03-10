package fr.davit.capturl.javadsl;

public abstract class Host {

    public abstract String address();

    public abstract boolean isEmpty();
    public abstract boolean isIPv4();
    public abstract boolean isIPv6();
    public abstract boolean isNamedHost();

    public static Host create(String address) {
        return fr.davit.capturl.scaladsl.Host$.MODULE$.apply(address);
    }
}
