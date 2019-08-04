package turing.client;

import jdk.jshell.spi.ExecutionControl;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import turing.client.io.ClientUserInterface;
import turing.client.io.TuringClientCommandLineInterface;
import turing.client.io.TuringParser;

import java.io.IOException;

public class TuringClient {
    private ClientUserInterface CLI;
    private TuringParser parser;

    public TuringClient() {
        CLI = new TuringClientCommandLineInterface();
        parser = new TuringParser();
    }

    public void start(String[] args) throws ExecutionControl.NotImplementedException {
        try {
            CommandLine line = parser.parse(args);
            if (line.hasOption("login")) {
                CLI.login(line.getOptionValues("login")[0], line.getOptionValues("login")[1]);
            }
            else if (line.hasOption("logout")) {
                CLI.logout();
            }
            else if (line.hasOption("create")) {
                String name = line.getOptionValues("create")[0];
                int sections = Integer.parseInt(line.getOptionValues("create")[1]);
                CLI.create(name, sections);
            }
            else if (line.hasOption("share")) {
                String documentName = line.getOptionValues("share")[0];
                String userName = line.getOptionValues("share")[1];
                CLI.share(documentName, userName);
            }
            // Show section of a document
            else if (line.hasOption("show") && line.getOptionValues("show").length == 2) {
                String documentName = line.getOptionValues("show")[0];
                int section = Integer.parseInt(line.getOptionValues("show")[1]);
                CLI.show(documentName, section);
            }
            // Show entire document
            else if (line.hasOption("show") && line.getOptionValues("show").length == 1) {
                String documentName = line.getOptionValue("show");
                CLI.show(documentName);
            }
            // Show list of documents
            else if (line.hasOption("list")) {
                CLI.list();
            }
            else { // interface misuse
                parser.printHelp();
            }
        }
        catch (ParseException | IOException e) {
            parser.printHelp();
        }
    }


}
