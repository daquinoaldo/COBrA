package com.aldodaquino.cobra.authorserver;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Map;

import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.ContentManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;


import org.apache.commons.cli.*;
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
        Options options = new Options();

        Option privateKeyOption = new Option("k", "private-key (required)", true,
                "private key to authenticate and deploy contents (default 8080)");
        privateKeyOption.setRequired(true);
        options.addOption(privateKeyOption);

        Option catalogOption = new Option("c", "catalog", true,
                "catalog address");
        catalogOption.setRequired(true);
        options.addOption(catalogOption);

        Option portOption = new Option("p", "port", true,
                "port on which running the author server");
        portOption.setRequired(false);
        options.addOption(portOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        String catalogAddress = null;
        int port = DEFAULT_PORT;
        try {
            cmd = parser.parse(options, args);

            privateKey = cmd.getOptionValue("private-key");
            if (privateKey == null || privateKey.length() != 64) throw new IllegalArgumentException("Empty private key");

            catalogAddress = cmd.getOptionValue("catalog");
            if (catalogAddress == null || catalogAddress.length() == 0)
                throw new IllegalArgumentException("Empty catalog address");

            String portS = cmd.getOptionValue("port");
            if (portS != null) port = Integer.parseInt(portS);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("author-server", options);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        // Init status
        credentials = Credentials.create(privateKey);
        catalogManager = new CatalogManager(credentials, catalogAddress);

        // Create server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

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