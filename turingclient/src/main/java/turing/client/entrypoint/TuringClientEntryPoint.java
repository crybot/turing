package turing.client.entrypoint;

import turing.client.TuringClient;

import java.io.IOException;

public class TuringClientEntryPoint {

    public static void main(String[] args) throws Exception {
        new TuringClient().start(args);
    }
}

