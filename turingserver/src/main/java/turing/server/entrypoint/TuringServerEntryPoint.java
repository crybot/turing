package turing.server.entrypoint;

import turing.server.TuringServer;

import java.io.IOException;
import java.io.ObjectInputStream;

public class TuringServerEntryPoint {

    public static void main(String[] args) {
        try (TuringServer server = new TuringServer(1024)) {
            server.start();
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
}
