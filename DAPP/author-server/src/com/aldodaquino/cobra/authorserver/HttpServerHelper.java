package com.aldodaquino.cobra.authorserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class HttpServerHelper {

    private static class HttpRequestHandler implements HttpHandler {
        BiConsumer<HttpExchange, Main.Status> consumer;
        Main.Status status;
        HttpRequestHandler(BiConsumer<HttpExchange, Main.Status> consumer, Main.Status status) {
            this.consumer = consumer;
            this.status = status;
        }
        @Override
        public void handle(HttpExchange request) {
            consumer.accept(request, status);
        }
    }

    static HttpHandler newHandler(BiConsumer<HttpExchange, Main.Status> consumer, Main.Status status) {
        return new HttpRequestHandler(consumer, status);
    }

    public static Map<String, String> parseGET(HttpExchange request) {
        String query = request.getRequestURI().getRawQuery();
        return parseQuery(query);
    }

    static Map<String, String> parsePOST(HttpExchange request) {
        String query = null;
        try {
            InputStreamReader isr = new InputStreamReader(request.getRequestBody(), "utf-8");
            query = new BufferedReader(isr).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseJson(query);
    }

    private static Map<String, String> parseJson(String json) {
        if (json == null || json.length() == 0) throw new IllegalArgumentException("Invalid query: null.");

        // HashMap to be filled with all parameters in the query
        Map<String, String> parameters = new HashMap<>();

        // remove parenthesis and quotes
        json = json.replace("{", "").replace("}", "").replaceAll("\"", "");

        // Split the query in pairs key=value
        String pairs[] = json.split("[;]");
        // Split each pair in key and value and put them in the Map
        for (String pair : pairs) {
            String param[] = pair.split("[:]");
            if (param.length > 0) {
                String key = param[0];
                String value = null;
                if (param.length > 1) value = param[1];
                parameters.put(key, value);
            }
        }

        return parameters;
    }

    private static Map<String, String> parseQuery(String query) {
        if (query == null || query.length() == 0) throw new IllegalArgumentException("Invalid query: null.");

        // HashMap to be filled with all parameters in the query
        Map<String, String> parameters = new HashMap<>();

        // Split the query in pairs key=value
        String pairs[] = query.split("[&]");
        // Split each pair in key and value and put them in the Map
        for (String pair : pairs) {
            String param[] = pair.split("[=]");
            if (param.length > 0) {
                try {
                    String key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                    String value = null;
                    if (param.length > 1) value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                    parameters.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return parameters;
    }

    static void sendResponse(HttpExchange request, String response, int code) {
        try {
            request.sendResponseHeaders(code, response.length());
            OutputStream os = request.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendResponse(HttpExchange request, String response) {
        sendResponse(request, response, 200);
    }

}