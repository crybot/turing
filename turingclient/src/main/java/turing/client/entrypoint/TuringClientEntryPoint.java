package turing.client.entrypoint;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import turing.client.io.TuringClientCommandLineInterface;
import turing.client.io.TuringParser;

import java.io.IOException;

public class TuringClientEntryPoint {

    public static void main(String[] args) throws IOException {
        TuringParser parser = new TuringParser();
        TuringClientCommandLineInterface CLI = new TuringClientCommandLineInterface();
        try {
            CommandLine line = parser.parse(args);
            if (line.hasOption("login")) {
                CLI.login(line.getOptionValues("login")[0], line.getOptionValues("login")[1]);
            }
            else if (line.hasOption("logout")) {
                CLI.logout();
            }
            else {
                CLI.printHelp();
            }
        }
        catch (ParseException e) {
            parser.printHelp();
        }


        // try {
        //     var socket = new Socket("localhost", 1024);
        //     var out = new JsonStreamWriter(socket.getOutputStream());
        //     var message1 = new JSONObject().put("login", new JSONObject()
        //             .put("user", "foo")
        //             .put("password", "bar"));

        //     out.write(message1);
        //     out.newLine();
        //     out.flush();
        //     socket.shutdownOutput();

        //     var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //     System.out.println(in.readLine());
        //     in.close();


        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }


}

