package com.aldodaquino.javautils;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestHelper {

    public static Map<String, Object> parseGET(HttpExchange request) throws IOException {
        String query = request.getRequestURI().getRawQuery();
        return parseQuery(query);
    }

    public static Map<String, Object> parsePOST(HttpExchange request) throws IOException {
        InputStreamReader isr = new InputStreamReader(request.getRequestBody(), "utf-8");
        String query = new BufferedReader(isr).readLine();
        return parseQuery(query);
    }

    private static Map<String, Object> parseQuery(String query) throws UnsupportedEncodingException {
        if (query == null) throw new IllegalArgumentException("Invalid query: null.");

        // HashMap to be filled with all parameters in the query
        Map<String, Object> parameters = new HashMap<>();

        // Split the query in pairs key=value
        String pairs[] = query.split("[&]");
        // Split each pair in key and value and put them in the Map
        for (String pair : pairs) {
            String param[] = pair.split("[=]");
            String key = null;
            String value = null;
            if (param.length > 0) key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
            if (param.length > 1) value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
            parameters.put(key, value);
        }

        return parameters;
    }

    public static void sendResponse(HttpExchange request, String response, int code) throws IOException {
        request.sendResponseHeaders(code, response.length());
        OutputStream os = request.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static void sendResponse(HttpExchange request, String response) throws IOException {
        sendResponse(request, response, 200);
    }

}