package com.aldodaquino.cobra.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import com.aldodaquino.cobra.main.Catalog;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.main.Stringifiable;
import com.aldodaquino.javautils.HttpRequestHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * HTTP Server for the REST Main
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Catalog catalog;

        // Get an existent catalog from address or create a new one
        if (args.length > 0)
            catalog = new Catalog(args[0]);
        else catalog = new Catalog();

        // create server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // set handlers
        server.createContext("/getAuthorContents", new GetAuthorContents(catalog));

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

    private static class GetAuthorContents implements HttpHandler  {
        Catalog catalog;
        GetAuthorContents(Catalog catalog) {
            this.catalog = catalog;
        }

        @Override
        public void handle(HttpExchange request) throws IOException {
            // get parameters
            Map<String, Object> parameters = HttpRequestHelper.parseGET(request);
            String author = (String) parameters.get("author");
            if (author == null) HttpRequestHelper.sendResponse(request, "ERROR: author address not specified.", 400);

            // get the list of content
            List<Content> authorContents = catalog.getAuthorContents(author);

            // send response
            String response = stringifyList(authorContents);
            HttpRequestHelper.sendResponse(request, response);
        }
    }
}