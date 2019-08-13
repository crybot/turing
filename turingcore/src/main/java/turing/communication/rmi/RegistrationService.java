package turing.communication.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrationService extends Remote {
    int REGISTRY_PORT = 1099;
    String SERVICE_NAME = "REGISTRATION-SERVICE";
    String register(String username, String password) throws RemoteException;
}
