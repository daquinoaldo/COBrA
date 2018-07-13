package com.aldodaquino.javautils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HttpHelper {

    private static CookieManager cookieManager = new CookieManager();

    private static class HttpRequestHandler implements HttpHandler {
        Consumer<HttpExchange> consumer;
        HttpRequestHandler(Consumer<HttpExchange> consumer) {
            this.consumer = consumer;
        }
        @Override
        public void handle(HttpExchange request) {
            consumer.accept(request);
        }
    }

    public static HttpHandler newHandler(Consumer<HttpExchange> consumer) {
        return new HttpRequestHandler(consumer);
    }

    public static Map<String, String> parseGET(HttpExchange request) {
        String query = request.getRequestURI().getRawQuery();
        return parseQuery(query);
    }

    public static Map<String, String> parsePOST(HttpExchange request) {
        String query = null;
        try {
            InputStreamReader isr = new InputStreamReader(request.getRequestBody(), "utf-8");
            query = new BufferedReader(isr).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseQuery(query);
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

    public static void sendResponse(HttpExchange request, String response) {
        sendResponse(request, response, 200);
    }

    public static Response makeGet(String url, Map<String, String> parameters) {
        return makeRequest(url + querifyParameters(parameters), "GET", "");
    }

    public static Response makePost(String url, Map<String, String> parameters) {
        return makeRequest(url, "POST", jsonifyParameters(parameters));
    }

    private static Response makeRequest(String url, String method, String parameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(parameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setRequestProperty("Cookie", joinCookie(cookieManager.getCookieStore().getCookies()));
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            //Send request
            if(!parameters.equals("")) {
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(parameters);
                outputStream.close();
            }

            // Get cookies
            String cookiesHeader = connection.getHeaderField("Set-Cookie");
            List<HttpCookie> cookies = HttpCookie.parse(cookiesHeader);
            cookies.forEach(cookie -> cookieManager.getCookieStore().add(null, cookie));

            //Get Response
            int status = connection.getResponseCode();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseData = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseData.append(line);
                responseData.append('\r');
            }
            bufferedReader.close();

            return new Response(status, responseData.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String joinCookie(List<?> list) {
        if (list == null || list.size() == 0) return null;
        final StringBuilder stringBuilder = new StringBuilder(list.size() * 16);
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) stringBuilder.append(";");
            Object elem = list.get(i);
            if (elem != null) stringBuilder.append(elem);
        }
        return stringBuilder.toString();
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
        public int code;
        public String data;
        private Response (int code, String data) {
            this.code = code;
            this.data = data;
        }
    }

}