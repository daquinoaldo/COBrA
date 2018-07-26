package com.aldodaquino.javautils;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains method that help to make http request.
 * Works with JSON body for POST request and query-style GET parameters.
 * @author Aldo D'Aquino.
 * @version 1.1.
 */
public class HttpHelper {

    /* SERVER SIDE */

    /**
     * Parse a GET request and return a Map<key, value>.
     * @param request the HttpExchange request received by the handler.
     * @return Map<String, String> containing the parameters.
     */
    public static Map<String, String> parseGET(HttpExchange request) {
        String query = request.getRequestURI().getRawQuery();
        if (query == null || query.length() == 0) throw new IllegalArgumentException("Invalid query: null.");
        return parseQuery(query);
    }

    /**
     * Parse a query and return a Map<key, value>.
     * @param query the String representing the query.
     * @return Map<String, String> containing the parameters.
     */
    public static Map<String, String> parseQuery(String query) {
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
        return parseJson(json);
    }

    /**
     * Parse a JSON and return a Map<key, value>.
     * @param json the String representing the stringified JSON.
     * @return Map<String, String> containing the parameters.
     */
    public static Map<String, String> parseJson(String json) {
        // parse the JSON body.
        if (json == null || json.length() == 0) throw new IllegalArgumentException("Invalid query: null.");

        // HashMap to be filled with all parameters in the query
        Map<String, String> parameters = new HashMap<>();

        // remove parenthesis and quotes
        json = json.replace("{", "").replace("}", "").replaceAll("\"", "");

        // Split the query in pairs key=value
        String pairs[] = json.split("[,]");
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

    /**
     * Make a GET request on the specified url.
     * @param url the url to be called.
     * @param parameters a map containing all the parameters that you want to be passed when the url is called.
     * @return a {@link Response} object.
     */
    public static Response makeGet(String url, Map<String, String> parameters) {
        return makeRequest(url + querifyParameters(parameters), "GET", "");
    }

    /**
     * Make a POST request on the specified url.
     * @param url the url to be called.
     * @param parameters a map containing all the parameters that you want to be passed in the body.
     * @return a {@link Response} object.
     */
    public static Response makePost(String url, Map<String, String> parameters) {
        return makeRequest(url, "POST", jsonifyParameters(parameters));
    }

    // internal function
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

    /**
     * Return a stringified JSON with the passed parameters.
     * @param parameters a Map<String, String> containing the parameters.
     * @return the stringified JSON.
     */
    public static String jsonifyParameters(Map<String, String> parameters) {
        if (parameters == null) return "";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        String separator = "";
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            stringBuilder.append(separator)
                    .append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            separator = ",";
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    /**
     * Return a string containing the parameters in the query format, ready to be appended to an url for a GET request.
     * @param parameters a Map<String, String> containing the parameters.
     * @return the String in the url format.
     */
    public static String querifyParameters(Map<String, String> parameters) {
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

    /**
     * Response class returned by the makeGet and makePost method.
     * Contains two field: the response code and the data String of the response.
     * @author Aldo D'Aquino.
     * @version 1.0.
     */
    public static class Response {
        public final int code;
        public final String data;
        private Response (int code, String data) {
            this.code = code;
            this.data = data;
        }

        /**
         * Returns this object as a stringified JSON.
         * @return a String representing the stringified JSON.
         */
        @Override
        public String toString() {
            return "{\"code\":\"" + code + "\",\"data\":\"" + data + "\"}";
        }
    }

}