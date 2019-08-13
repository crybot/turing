package turing.server.communication.rmi;

import turing.communication.rmi.RegistrationService;
import turing.model.user.User;
import turing.server.persistence.UserDataManager;
import turing.server.state.ServerState;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

public class RemoteRegistrationService extends RemoteServer implements RegistrationService {
    private ServerState serverState;
    private UserDataManager userDataManager;

    public RemoteRegistrationService(ServerState serverState) {
        this.serverState = serverState;
        userDataManager = new UserDataManager("./model/user/users");
    }

    //TODO: return a more sophisticated response (maybe including a boolean status code)
    @Override
    public String register(String username, String password) throws RemoteException {
        System.out.println("Remote registration service invoked");

        if (userDataManager.getByName(username).isPresent()) {
            return "User " + username + " already registered.";
        }
        userDataManager.create(new User(username, password));
        return "User successfully registered";
    }
}
