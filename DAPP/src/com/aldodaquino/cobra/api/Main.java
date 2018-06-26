package com.aldodaquino.cobra.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import com.aldodaquino.javautils.HttpRequestHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * HTTP Server for the REST Main
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // create server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // set handlers
        server.createContext("/getAuthorContents", new GetAuthorContents());

        // start server
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class GetAuthorContents implements HttpHandler {
        @Override
        public void handle(HttpExchange request) throws IOException {
            Map<String, Object> parameters = HttpRequestHelper.parseGET(request);

            // send response
            String response = "";
            HttpRequestHelper.sendResponse(request, response);
        }
    }

}