package com.aldodaquino.cobra.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.main.Stringifiable;
import com.aldodaquino.javautils.HttpRequestHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.web3j.crypto.Credentials;

/**
 * HTTP Server for the REST MainAPI
 */
public class MainAPI {

    private static final int PORT = 8000;
    public CatalogManager catalogManager;

    /**
     * Main method.
     * @param args first argument th private key of an account,
     *             second argument, optional, the address of an existent catalog.
     * @throws IOException if the web server cannot be created on the specified port
     */
    public static void main(String[] args) throws IOException {
        CatalogManager catalogManager;

        // Get an existent catalog from address or create a new one
        if (args.length == 0) throw new IllegalArgumentException(
                "Usage: args[0] private key, args[1], optional, an existent CatalogContract address.");
        Credentials credentials = Credentials.create(args[0]);
        if (args.length > 1)
            catalogManager = new CatalogManager(credentials, args[1]);
        else catalogManager = new CatalogManager(credentials);

        // create server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // set handlers
        server.createContext("/getAuthorContents", new GetAuthorContents(catalogManager));

        // start server
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    /**
     * Auxiliary function: stringify a list of Stringifiable objects.
     * @param list the list of objects that implement the Stringifiable interface.
     * @return a JSON string.
     */
    private static <T extends Stringifiable> String stringifyList(List<T> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (T element : list)
            stringBuilder.append(element.stringify()).append(",");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * Send a JSON string containing the list of Contents of a specified author.
     */
    private static class GetAuthorContents implements HttpHandler  {
        CatalogManager catalogManager;
        GetAuthorContents(CatalogManager catalogManager) {
            this.catalogManager = catalogManager;
        }

        @Override
        public void handle(HttpExchange request) throws IOException {
            // get parameters
            Map<String, Object> parameters = HttpRequestHelper.parseGET(request);
            String author = (String) parameters.get("author");
            if (author == null) HttpRequestHelper.sendResponse(request, "ERROR: author address not specified.", 400);

            // get the list of content
            List<Content> authorContents = catalogManager.getAuthorContents(author);

            // send response
            String response = stringifyList(authorContents);
            HttpRequestHelper.sendResponse(request, response);
        }
    }

    /**
     * Deploy a CatalogContract.
     */
    private static class DeployCatalog implements HttpHandler  {
        MainAPI main;
        DeployCatalog(MainAPI main) {
            this.main = main;
        }

        @Override
        public void handle(HttpExchange request) throws IOException {

            // get parameters
            Map<String, Object> parameters = HttpRequestHelper.parseGET(request);
            String address = (String) parameters.get("address");
            if (address == null) HttpRequestHelper.sendResponse(request, "ERROR: address not specified.", 400);

            // get the list of content
            Credentials credentials = Credentials.create(address);
            main.catalogManager = new CatalogManager(credentials);

            // send response
            String response = main.catalogManager.getAddress();
            HttpRequestHelper.sendResponse(request, response);
        }
    }
}