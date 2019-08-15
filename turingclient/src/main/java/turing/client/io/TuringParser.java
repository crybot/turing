package turing.client.io;

import org.apache.commons.cli.*;

public class TuringParser extends DefaultParser {
    private Options options;
    private HelpFormatter formatter;

    public TuringParser() {
        options = setupOptions();
        formatter = new HelpFormatter();
    }

    public void printHelp() {
        formatter.printHelp("turing", options);
    }

    private Options setupOptions() {
        Options options = new Options();

        /* Help */
        options.addOption("help", "mostra questo messaggio di help");

        /* Users */
        options.addOption(Option.builder("register")
                .numberOfArgs(2)
                .argName("username> <password")
                .desc("registra l'utente")
                .build());
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
        options.addOption("list", false, "mostra la lista dei documenti");
        options.addOption(Option.builder("edit")
                .numberOfArgs(2)
                .argName("doc> <sec")
                .desc("modifica una sezione del documento")
                .build());
        options.addOption(Option.builder("endedit")
                .numberOfArgs(2)
                .argName("doc> <sec")
                .desc("fine modifica della sezione del documento")
                .build());

        /* Chat */
        options.addOption(Option.builder("send")
                .numberOfArgs(1)
                .argName("msg")
                .desc("invia un messaggio sullla chat")
                .build());
        options.addOption("receive", "visualizza i messaggi ricevuti sulla chat");

        return options;
    }

    public CommandLine parse(String[] args) throws ParseException {
        return parse(options, args);
    }
}
