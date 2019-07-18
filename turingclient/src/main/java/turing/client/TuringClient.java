package turing.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import turing.client.io.ClientUserInterface;
import turing.client.io.TuringClientCommandLineInterface;
import turing.client.io.TuringParser;

public class TuringClient {
    private ClientUserInterface CLI;
    private TuringParser parser;

    public TuringClient() {
        CLI = new TuringClientCommandLineInterface();
        parser = new TuringParser();
    }

    public void start(String[] args) {
        try {
            CommandLine line = parser.parse(args);
            if (line.hasOption("login")) {
                CLI.login(line.getOptionValues("login")[0], line.getOptionValues("login")[1]);
            }
            else if (line.hasOption("logout")) {
                CLI.logout();
            }
            else { // interface misuse
                CLI.printHelp();
            }
        }
        catch (ParseException e) {
            parser.printHelp();
        }
    }


}
