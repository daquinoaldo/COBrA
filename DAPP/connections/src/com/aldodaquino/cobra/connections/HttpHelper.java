package com.aldodaquino.cobra.connections;

import com.aldodaquino.cobra.main.Status;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class HttpHelper {

    /* SERVER SIDE */
    private static class HttpRequestHandler implements HttpHandler {
        final BiConsumer<HttpExchange, Status> consumer;
        final Status status;
        HttpRequestHandler(BiConsumer<HttpExchange, Status> consumer, Status status) {
            this.consumer = consumer;
            this.status = status;
        }
        @Override
        public void handle(HttpExchange request) {
            consumer.accept(request, status);
        }
    }

    /**
     * Return new HttpHandler
     * @param consumer a function to be called when a new request arrive.
     *                 The consumer has to accept the HttpExchange request and the Status.
     * @param status to be passed to the consumer.
     * @return HttpHandler, the handler.
     */
    public static HttpHandler newHandler(BiConsumer<HttpExchange, Status> consumer, Status status) {
        return new HttpRequestHandler(consumer, status);
    }

    /**
     * Parse a GET request and return a Map<key, value>.
     * @param request the HttpExchange request received by the handler.
     * @return Map<String, String> containing the parameters.
     */
    public static Map<String, String> parseGET(HttpExchange request) {
        String query = request.getRequestURI().getRawQuery();
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

    /**
     * Parse a POST request and return a Map<key, value>.
     * @param request the HttpExchange request received by the handler.
     * @return Map<String, String> containing the parameters.
     */
    public static Map<String, String> parsePOST(HttpExchange request) {
        String json = null;
        try {
            InputStreamReader isr = new InputStreamReader(request.getRequestBody(), "utf-8");
            json = new BufferedReader(isr).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse the JSON body.
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

    /**
     * Send a response to an HttpExchange request.
     * @param request the request.
     * @param response a String containing the response.
     * @param code the status code of the response.
     */
    public static void sendResponse(HttpExchange request, String response, int code) {
        try {
            request.sendResponseHeaders(code, response.length());
            OutputStream os = request.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a response to an HttpExchange request.
     * @param request the request.
     * @param response a String containing the response..
     */
    public static void sendResponse(HttpExchange request, String response) {
        sendResponse(request, response, 200);
    }


    /* CLIENT SIDE */

    public static Response makeGet(String url, Map<String, String> parameters) {
        return makeRequest(url + querifyParameters(parameters), "GET", "");
    }

    public static Response makePost(String url, Map<String, String> parameters) {
        return makeRequest(url, "POST", jsonifyParameters(parameters));
    }

    private static Response makeRequest(String url, String method, String parameters) {
        HttpURLConnection connection = null;
        int status = -1;
        try {
            //Create connection
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(parameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            //Send request
            if(!parameters.equals("")) {
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(parameters);
                outputStream.close();
            }

            //Get Response
            status = connection.getResponseCode();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseData = new StringBuilder();
            String line;
            String separator = "";
            while ((line = bufferedReader.readLine()) != null) {
                responseData.append(separator).append(line);
                separator = "\n";
            }
            bufferedReader.close();

            return new Response(status, responseData.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
            return status < 0 ? null : new Response(status, "");
        }
        finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String jsonifyParameters(Map<String, String> parameters) {
        if (parameters == null) return "";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        String separator = "";
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            stringBuilder.append(separator)
                    .append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            separator = ";";
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private static String querifyParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) return "";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("?");
        String separator = "";
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            stringBuilder.append(separator)
                    .append(entry.getKey()).append("=").append(entry.getValue());
            separator = "&";
        }
        return stringBuilder.toString();
    }

    public static class Response {
        public final int code;
        public final String data;
        private Response (int code, String data) {
            this.code = code;
            this.data = data;
        }
        @Override
        public String toString() {
            return "{\"code\":\"" + code + "\",\"data\":\"" + data + "\"}";
        }
    }

}