package turing.network;

/**
 * A network-aware entity: might be a User, a Server, etc.
 */
public abstract class Entity {
    //TODO: define structure
    private Address address;

    protected Entity(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }
}
