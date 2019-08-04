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

        /* Users */
        // options.addOption("register", true, "registra l'utente");
        options.addOption(Option.builder("login")
                .numberOfArgs(2)
                .argName("username> <password")
                .desc("effettua il login")
                .build());
        options.addOption("logout", false, "effettua il logout");

        /* Documents */
        options.addOption(Option.builder("create")
                .numberOfArgs(2)
                .argName("doc> <numsezioni")
                .desc("crea un documento")
                .build());
        options.addOption(Option.builder("share")
                .numberOfArgs(2)
                .argName("doc> <username")
                .desc("condivide il documento")
                .build());
        options.addOption(Option.builder("show")
                .numberOfArgs(2)
                .optionalArg(true) // <sec> is optional
                .argName("doc> <sec")
                .desc("mostra una sezione del documento")
                .build());

        return options;
    }

    public CommandLine parse(String[] args) throws ParseException {
        return parse(options, args);
    }
}
