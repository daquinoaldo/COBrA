package com.aldodaquino.cobra.authorserver;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;

import com.aldodaquino.cobra.connections.FileExchange;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.ContentManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.web3j.crypto.Credentials;

/**
 * HTTP Server for the REST Main
 */
public class Main {

    private static final int DEFAULT_PORT = 8080;
    private static final String CONTENT_FILE_PATH = "author_content_files/";

    static class Status {
        String privateKey;
        Credentials credentials;
        CatalogManager catalogManager;
        String hostname;
        int port;
    }

    /**
     * Main method.
     */
    public static void main(String[] args) throws IOException {

        // Parse cmd options
        CliHelper cliHelper = new CliHelper();
        cliHelper.addOption("h", "help", false, "Print this help message.");
        cliHelper.addOption("k", "private-key", true,
                "Private key of your account (required).");
        cliHelper.addOption("c", "catalog", true, "Catalog address (required).");
        cliHelper.addOption("n", "hostname", true,
                "Name of this host, i.e. the IP address of this server, used to deploy content (required).");
        cliHelper.addOption("p", "port", true,
                "Port on which run the server. Default: " + DEFAULT_PORT + ".");
        cliHelper.parse(args);

        if (cliHelper.isPresent("h")) {
            System.out.println(cliHelper.getHelpMessage());
            System.exit(0);
        }

        Status status = new Main.Status();

        status.privateKey = cliHelper.getValue("private-key");
        if (status.privateKey == null || status.privateKey.length() == 0) {
            System.err.println(cliHelper.getMissingOptionMessage("private-key"));
            System.err.flush();
            System.out.println(cliHelper.getHelpMessage());
            System.out.flush();
            System.exit(1);
        }

        String catalogAddress = cliHelper.getValue("catalog");
        if (catalogAddress == null || catalogAddress.length() == 0) {
            System.err.println(cliHelper.getMissingOptionMessage("catalog"));
            System.err.flush();
            System.out.println(cliHelper.getHelpMessage());
            System.out.flush();
            System.exit(1);
        }

        status.hostname = cliHelper.getValue("hostname");
        if (status.hostname == null || status.hostname.length() == 0) {
            System.err.println(cliHelper.getMissingOptionMessage("hostname"));
            System.err.flush();
            System.out.println(cliHelper.getHelpMessage());
            System.out.flush();
            System.exit(1);
        }

        String portS = cliHelper.getValue("port");
        status.port = portS != null && portS.length() != 0 ? Integer.parseInt(portS) : DEFAULT_PORT;

        // Init status
        status.credentials = Credentials.create(status.privateKey);
        status.catalogManager = new CatalogManager(status.credentials, catalogAddress);

        // Create server
        HttpServer server = HttpServer.create(new InetSocketAddress(status.port), 0);
        System.out.println("Server running on port " + status.port + ".\n");

        // set handlers
        server.createContext("/deploy", HttpServerHelper.newHandler(Main::deploy, status));
        server.createContext("/access", HttpServerHelper.newHandler(Main::access, status));

        // start server
        server.setExecutor(null); // creates a default executor
        server.start();
    }


    /**
     * Handler for the /deploy url.
     * @param request a POST request with JSON encoded data containing:
     *                privateKey of the author;
     *                name of the content;
     *                genre of the content (can be null);
     *                price of the content (if null is set to 0);
     *                port on which is running the server socket that uploads the file.
     */
    private static void deploy(HttpExchange request, Status status) {
        // get parameters
        Map<String, String> parameters = HttpServerHelper.parsePOST(request);

        if (!status.privateKey.equals(parameters.get("privateKey"))) {
            HttpServerHelper.sendResponse(request, "Only the author server owner can perform this action." +
                    "You must login with the same private key of the server.", 403);
            return;
        }

        String name = parameters.get("name");
        if (name == null) {
            HttpServerHelper.sendResponse(request, "ERROR: name not specified.", 400);
            return;
        }

        String genre = parameters.get("genre");

        String priceS = parameters.get("price");
        BigInteger price;
        try {
            price = new BigInteger(priceS.length() != 0 ? priceS : "0");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            HttpServerHelper.sendResponse(request, "ERROR: Invalid price.\n" + e.getMessage(), 400);
            return;
        }

        String hostname = request.getRemoteAddress().getHostName();

        String portS = parameters.get("port");
        int port;
        try {
            port = Integer.parseInt(portS);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            HttpServerHelper.sendResponse(request, "ERROR: Invalid port number.\n" + e.getMessage(), 400);
            return;
        }

        // deploy the content
        String address;
        try {
            ContentManager contentManager = new ContentManager(status.credentials,
                    status.catalogManager.getAddress(), name, genre, price, status.hostname, status.port);
            address = contentManager.getAddress();
        } catch (Exception e) {
            e.printStackTrace();
            HttpServerHelper.sendResponse(request, e.getMessage(), 400);
            return;
        }

        // download the file
        File file = new File(CONTENT_FILE_PATH + address);
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        FileExchange.receiveFile(file, hostname, port);

        // send the response
        HttpServerHelper.sendResponse(request, address);
    }

    /**
     * Handler for the /access url.
     * @param request a POST request with JSON encoded data containing:
     *                privateKey of the author
     *                name of the content
     *                genre of the content (can be null)
     *                price of the content (if null is set to 0)
     */
    private static void access(HttpExchange request, Status status) {
        // get parameters
        Map<String, String> parameters = HttpServerHelper.parsePOST(request);

        String address = parameters.get("address");
        if (address == null) {
            HttpServerHelper.sendResponse(request, "ERROR: content address not specified.", 400);
            return;
        }

        String userPrivateKey = parameters.get("privateKey");
        if (userPrivateKey == null) {
            HttpServerHelper.sendResponse(request, "ERROR: user private key not specified.", 400);
            return;
        }
        Credentials credentials = Credentials.create(userPrivateKey);
        String user = credentials.getAddress();

        if (!status.catalogManager.hasAccess(address, user)) {
            HttpServerHelper.sendResponse(request, "ERROR: you don't have access to this content.", 400);
            return;
        }

        // open the socket for the file
        ServerSocketChannel serverSocketChannel = FileExchange.openFileSocket();
        if (serverSocketChannel == null) {
            HttpServerHelper.sendResponse(request, "ERROR: cannot open the server socket.", 500);
            return;
        }

        int port = serverSocketChannel.socket().getLocalPort();
        FileExchange.startFileSender(serverSocketChannel, new File(CONTENT_FILE_PATH + address),
                () -> System.out.println("User " + user + " has received all the content " + address + "."));

        // consume the content
        ContentManager contentManager = new ContentManager(credentials, address);
        if (!contentManager.consumeContent()) {
            HttpServerHelper.sendResponse(request, "ERROR: cannot consume content.", 500);
            return;
        }

        // communicate the port number
        HttpServerHelper.sendResponse(request, Integer.toString(port));
    }

}