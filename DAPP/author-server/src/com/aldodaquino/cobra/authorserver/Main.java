package com.aldodaquino.cobra.authorserver;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Map;

import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.ContentManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.web3j.crypto.Credentials;

/**
 * HTTP Server for the REST Main
 */
public class Main {

    private static final int DEFAULT_PORT = 8000;

    private static String privateKey;
    private static Credentials credentials;
    private static CatalogManager catalogManager;

    /**
     * Main method.
     */
    public static void main(String[] args) throws IOException {

        // Parse cmd options
        CliHelper cliHelper = new CliHelper();
        cliHelper.addOption("h", "help", false, "Print this help message.");
        cliHelper.addOption("c", "catalog", true, "Catalog address.");
        cliHelper.addOption("k", "private-key", true, "Private key of your account.");
        cliHelper.addOption("p", "port", true, "Port on which run the server.");
        cliHelper.parse(args);

        if (cliHelper.isPresent("h")) System.out.println(cliHelper.getHelpMessage());

        privateKey = cliHelper.getValue("private-key");
        if (privateKey == null || privateKey.length() == 0) {
            System.err.println(cliHelper.getMissingOptionMessage("private-key"));
            System.err.flush();
            System.out.println(cliHelper.getHelpMessage());
            System.out.flush();
        }

        String catalogAddress = cliHelper.getValue("catalog");
        if (catalogAddress == null || catalogAddress.length() == 0) {
            System.err.println(cliHelper.getMissingOptionMessage("catalog"));
            System.err.flush();
            System.out.println(cliHelper.getHelpMessage());
            System.out.flush();
        }

        String portS = cliHelper.getValue("port");
        int port = portS != null && portS.length() != 0 ? Integer.parseInt(portS) : DEFAULT_PORT;

        // Init status
        credentials = Credentials.create(privateKey);
        catalogManager = new CatalogManager(credentials, catalogAddress);

        // Create server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Server running on port " + port);

        // set handlers
        server.createContext("/deploy", HttpHelper.newHandler(Main::deploy));

        // start server
        server.setExecutor(null); // creates a default executor
        server.start();
    }


    /**
     * Handler for the /deploy url.
     * @param request a POST request with JSON encoded data containing:
     *                privateKey of the author
     *                name of the content
     *                genre of the content (can be null)
     *                price of the content (if null is set to 0)
     */
    private static void deploy(HttpExchange request) {
        // get parameters
        Map<String, String> parameters = HttpHelper.parsePOST(request);

        if (!isOwner(parameters.get("privateKey")))
            HttpHelper.sendResponse(request, "Only the author server owner can perform this action." +
                    "You must login with the same private key of the server.", 403);

        String name = parameters.get("name");
        if (name == null) HttpHelper.sendResponse(request, "ERROR: name not specified.", 400);

        String genre = parameters.get("genre");

        String priceS = parameters.get("price");
        BigInteger price;
        try {
            price = new BigInteger(priceS.length() != 0 ? priceS : "0");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            HttpHelper.sendResponse(request, e.getMessage(), 400);
            return;
        }

        try {
            ContentManager contentManager = new ContentManager(credentials, catalogManager.getAddress(), name, genre, price);
            HttpHelper.sendResponse(request, contentManager.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
            HttpHelper.sendResponse(request, e.getMessage(), 400);
        }
    }

    private static boolean isOwner(String privateKey) {
        return privateKey != null && privateKey.length() == 64 && Main.privateKey.equals(privateKey);
    }

}