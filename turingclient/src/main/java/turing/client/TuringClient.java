package turing.client;

import turing.client.io.ClientUserInterface;
import turing.client.io.TuringClientCommandLineInterface;

public class TuringClient {
    private ClientUserInterface userInterface;

    public TuringClient() {
        userInterface = new TuringClientCommandLineInterface();
    }


}
