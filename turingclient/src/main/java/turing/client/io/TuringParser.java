package turing.client.io;

import org.apache.commons.cli.*;

public class TuringParser extends DefaultParser {
    private Options options;
    HelpFormatter formatter;

    public TuringParser() {
        options = setupOptions();
        formatter = new HelpFormatter();
    }

    public void printHelp() {
        formatter.printHelp("turing", options);
    }

    private Options setupOptions() {
        Options options = new Options();
        options.addOption("help", "mostra questo messaggio di help");
        // options.addOption("register", true, "registra l'utente");
        // options.addOption("login", true, "effettua il login");
        // options.addOption("logout", "effettua il logout");

        options.addOption(Option.builder("login")
                .numberOfArgs(2)
                .argName("username> <password")
                .desc("effettua il login")
                .build());

        return options;
    }

    public CommandLine parse(String[] args) throws ParseException {
        return parse(options, args);
    }
}
