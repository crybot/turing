package turing.client.entrypoint;

import turing.client.TuringClient;

import java.io.IOException;

public class TuringClientEntryPoint {

    public static void main(String[] args) throws IOException {
        new TuringClient().start(args);
    }
}

