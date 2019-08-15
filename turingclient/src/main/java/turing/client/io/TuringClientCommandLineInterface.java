package turing.client.io;

import jdk.jshell.spi.ExecutionControl;
import org.json.JSONObject;
import turing.communication.rmi.RegistrationService;
import turing.communication.tcp.TcpCommunication;
import turing.communication.tcp.TcpMessage;
import turing.communication.udp.UdpMessage;
import turing.model.document.Document;
import turing.model.invitation.Invitation;
import turing.model.user.User;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;
import java.util.UUID;

/**
 * Exposes and implements Client services for the Turing application
 */
//TODO: move caching logic into TuringClient. It does not make sense to handle it inside the CLI interface.
//TODO: refactor code
public class TuringClientCommandLineInterface implements ClientUserInterface {
    private static final File cachedUserId = new File("./cache/userId");
    private RegistrationService registrationService;
    private InetAddress serverAddress; //TODO: use this instead of InetAddress.getLocalHost() (search and replace)

    public TuringClientCommandLineInterface(InetAddress serverAddress) throws Exception {
        this.serverAddress = serverAddress;
        Registry registry = LocateRegistry.getRegistry(serverAddress.getHostAddress(),
                RegistrationService.REGISTRY_PORT);
        registrationService = (RegistrationService) registry.lookup(RegistrationService.SERVICE_NAME);
    }

    private File getSectionFile(String documentName, int section) {
        return new File("./cache/" + documentName + "/" + section);
    }

    /**
     * Store the content of the section being edited in a persistent location for later work.
     * @param documentName  name of the document whose section is being edited
     * @param section       number of the section being edited
     * @param content       content of the section bein edited
     * @throws IOException  if the cached file does not exist and cannot be created
     */
    // TODO: move into turing-core: persistence.Persistence.store(bytes[] content, File file)
    private void saveSection(String documentName, int section, String content) throws IOException {
        File sectionFile = getSectionFile(documentName, section);
        // Create section file (and its parent directories) if it does not exist
        if (!sectionFile.exists()) {
            sectionFile.getParentFile().mkdirs();
            Files.createFile(sectionFile.toPath());
        }
        ByteBuffer buffer = ByteBuffer.wrap(content.getBytes());
        FileChannel channel = new FileOutputStream(sectionFile).getChannel();
        channel.write(buffer);
        channel.close();
    }

    // TODO: move into turing-core: persistence.Persistence.retrieve(File file)
    private Optional<String> getSavedSection(String documentName, int section) throws IOException {
        File sectionFile = getSectionFile(documentName, section);
        if (!sectionFile.exists()) {
            return Optional.empty();
        }
        try(FileChannel channel = FileChannel.open(sectionFile.toPath())) {
            var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, sectionFile.length());
            // Decode the bytebuffer with the default charset
            Charset charset = Charset.defaultCharset();
            CharsetDecoder decoder = charset.newDecoder();

            String content = decoder.decode(buffer).toString();
            return Optional.of(content);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Store users's UUID in a persistent location for later work.
     * The stored ID is transparently used by the logout procedure.
     * Only one user at a time can store his ID in the cached file.
     * @param userId    the UUID to be stored
     * @throws IOException  if the credentials file does not exist and cannot be created
     */
    private void saveUserId(UUID userId) throws IOException {
        // Create userId file (and its parent directories) if it does not exist
        if (!cachedUserId.exists()) {
            cachedUserId.getParentFile().mkdirs();
            Files.createFile(cachedUserId.toPath());
        }
        ByteBuffer buffer = ByteBuffer.wrap(userId.toString().getBytes());
        FileChannel channel = new FileOutputStream(cachedUserId).getChannel();
        channel.write(buffer);
        channel.close();
    }

    /**
     * Retrieve the previously stored user's UUID
     * @return  user's ID wrapped in an Optional.
     */
    private Optional<UUID> getSavedUserId() {
        if (!cachedUserId.exists()) {
            return Optional.empty();
        }
        try(FileChannel channel = FileChannel.open(cachedUserId.toPath())) {
            var buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, cachedUserId.length());
            // Decode the bytebuffer with the default charset
            Charset charset = Charset.defaultCharset();
            CharsetDecoder decoder = charset.newDecoder();

            UUID userId = UUID.fromString(decoder.decode(buffer).toString());
            return Optional.of(userId);
        }
        catch (Exception e) {
            return Optional.empty();
        }

    }

    //TODO: try erasing cached User ID and then make a request
    private Optional<String> sendRequest(String requestName, JSONObject parameters, String successString,
                                         String failureString, boolean includeAuth) {
        try {
            // Attach UserId (Auth) to the request body
            if (includeAuth) {
                Optional<UUID> userId = getSavedUserId();
                if (!userId.isPresent()) {
                    throw new IllegalStateException("Devi prima effettuare il login.");
                }
                else {
                    parameters.put("userId", userId.get().toString());
                }
            }

            // make payload
            JSONObject payload = new JSONObject().put(requestName, parameters);

            // Establish communication channel with the server
            var communication = new TcpCommunication(InetAddress.getLocalHost(), 1024);
            // Send request message to the server
            communication.sendMessage(TcpMessage.makeRequest(payload));
            // Receive response from the server
            Optional<TcpMessage> response = communication.consumeMessage();

            // If any response has been received
            if (response.isPresent()) {
                // If the response contains any content
                if (response.get().getOk()) {
                    System.out.println(successString);
                    return response.get().getResponse();
                }
                else {
                    System.err.println(failureString + ": "
                            + response.get().getResponse().orElse("Errore non specificato"));
                    return Optional.empty();
                }
            }
        }
        catch (Exception e) {
            System.err.println(failureString + ": " + e.getLocalizedMessage());
        }

        return Optional.empty();
    }

    /**
     * Invoke the remote registration service published by the server and print the response to screen.
     * @param username
     * @param password
     */
    @Override
    public void register(String username, String password) {
        try {
            String response = registrationService.register(username, password);
            System.out.println(response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /**
     * Send a login request to the server and wait for a response.
     * If any response is received, it will be printed on screen.
     * Note: In case of a successful login, the server returns an unique ID to use as an authentication token for
     *       subsequent requests.
     * @param username  the username of the user
     * @param password  the password of the user
     */
    @Override
    public void login(String username, String password) throws IOException {
        var parameters = new User(username, password).toJson();
        Optional<String> response = sendRequest("login", parameters,
                "Login effettuato con successo",
                "Impossibile effettuare il login",
                false);
        if (response.isPresent()) {
            saveUserId(UUID.fromString(response.get()));
        }
    }

    /**
     * Send a login request to the server and wait for a response.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     */
    @Override
    public void logout() {
        var parameters = new JSONObject();
        sendRequest("logout", parameters,
                "Logout effettuato con successo",
                "Impossibile effettuare il logout",
                true);
    }

    /**
     * Send a 'create document' request to the server
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param document
     * @param sections
     */
    @Override
    public void create(String document, int sections) {
        Optional<UUID> userId = getSavedUserId();
        if (!userId.isPresent()) {
            System.out.println("Devi eseguire il login prima di poter effettuare un'operazione");
        }
        else {
            var doc = new Document(document, sections, userId.get());
            var parameters = doc.toJson();
            sendRequest("create", parameters,
                    "Documento creato con successo",
                    "Impossibile creare documento",
                    false);
        }
    }

    /**
     * Send a 'share document' request to the server
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param document
     * @param username
     */
    @Override
    public void share(String document, String username) {
        var invite = new Invitation(document, username);
        var parameters = invite.toJson();
        sendRequest("share", parameters,
                "Documento condiviso con successo",
                "Impossibile condividere documento",
                true);
    }

    /**
     * Send a 'show section' request to the server
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param documentName
     * @param section
     */
    @Override
    public void show(String documentName, int section) {
        var parameters = new JSONObject()
                .put("documentName", documentName)
                .put("section", section);
        Optional<String> response = sendRequest("show", parameters,
                "Contenuto della sezione " + section + ": ",
                "Impossibile visualizzare sezione",
                true);
        response.ifPresent(System.out::println);
    }

    /**
     * Send a 'show document' request to the server
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param documentName
     */
    @Override
    public void show(String documentName) {
        var parameters = new JSONObject().put("documentName", documentName);
        Optional<String> response = sendRequest("show", parameters,
                "Contenuto del documento: ",
                "Impossibile visualizzare documento",
                true);
        response.ifPresent(System.out::println);
    }

    /**
     * Send a 'list documents' request to the server.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     */
    @Override
    public void list() {
        var parameters = new JSONObject();
        Optional<String> response = sendRequest("list", parameters,
                "Documenti modificabili: ",
                "Impossibile visualizzare la lista dei documenti",
                true);
        response.ifPresent(System.out::println);
    }

    /**
     * Send an 'edit section' request to the server.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param document
     * @param section
     */
    @Override
    public void edit(String document, int section) throws IOException {
        var parameters = new JSONObject()
                .put("documentName", document)
                .put("section", section);
        Optional<String> response = sendRequest("edit", parameters,
                "Sezione " + section + " del documento " + document + " scaricata con successo",
                "Impossibile modificare la sezione",
                true);

        // If a response is present (meaning the request was successful)
        if (response.isPresent()) {
            // Save the section content into a a file on the client machine
            saveSection(document, section, response.get());
            // Get the path of the file and open an editor (gedit by deafult, because it is commonly installed on most
            // machines)
            File sectionFile = getSectionFile(document, section);
            ProcessBuilder builder = new ProcessBuilder(
                    "gedit", sectionFile.getAbsolutePath());
            Process editorProcess = builder.start();
            /**
             * Uncomment this to let the client wait for the editor to close
             */
            // try {
            //     editorProcess.waitFor();
            // }
            // catch (InterruptedException e) {
            // }
        }
    }

    /**
     * Send an 'end edit section' request to the server.
     * If any response is received, it will be printed on screen.
     * The credentials to be sent to the server have been previously cached.
     * @param document
     * @param section
     */
    @Override
    public void endEdit(String document, int section) throws IOException {
        var content = getSavedSection(document, section);
        if (!content.isPresent()) {
            throw new IOException("Impossibile trovare la sezione richiesta, potrebbe essere stata eliminata.");
        }
        var parameters = new JSONObject()
                .put("documentName", document)
                .put("section", section)
                .put("content", content.get());
        sendRequest("endEdit", parameters,
                "Sezione " + section + " del documento " + document + " aggiornata con successo",
                "Impossibile modificare la sezione",
                true);
    }

    /**
     *
     * @param message
     * @throws IOException
     */
    @Override
    public void send(String message) throws IOException {
        var userId = getSavedUserId();
        if (userId.isPresent()) {
            var parameters = new JSONObject().put("message", message).put("userId", userId.get());
            var payload = new JSONObject().put("send", parameters);

            var socket = new DatagramSocket();
            var packet = new DatagramPacket(payload.toString().getBytes(),
                    payload.toString().getBytes().length,
                    InetAddress.getLocalHost(),
                    8192);
            socket.send(packet);

            // Wait for a response
            packet = new DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);
            var udpMessage = UdpMessage.makeResponse(packet.getData());
            if (udpMessage.getResponse().isPresent()) {
                if (udpMessage.getOk()) {
                    System.out.println("Messaggio inviato: " + udpMessage.getResponse().get());
                }
                else {
                    System.out.println("Errore: " + udpMessage.getResponse().get());
                }
            }

            socket.close();
        }
        else {
            System.err.println("Devi prima effettuare il login");
        }
    }

    /**
     *
     * @throws IOException
     */
    //TODO: factorize (very similar to send())
    @Override
    public void receive() throws IOException {
        var userId = getSavedUserId();
        if (userId.isPresent()) {
            var parameters = new JSONObject().put("userId", userId.get());
            var payload = new JSONObject().put("receive", parameters);

            var socket = new DatagramSocket();
            var packet = new DatagramPacket(payload.toString().getBytes(),
                    payload.toString().getBytes().length,
                    InetAddress.getLocalHost(),
                    8192);
            socket.send(packet);

            // Wait for a response
            packet = new DatagramPacket(new byte[8192], 8192);
            socket.receive(packet);
            var udpMessage = UdpMessage.makeResponse(packet.getData());
            if (udpMessage.getResponse().isPresent()) {
                if (udpMessage.getOk()) {
                    System.out.println("Messaggi ricevuti:" + System.lineSeparator() + udpMessage.getResponse().get());
                }
                else {
                    System.out.println("Errore: " + udpMessage.getResponse().get());
                }
            }

            socket.close();
        }
        else {
            System.err.println("Devi prima effettuare il login");
        }
    }
}
