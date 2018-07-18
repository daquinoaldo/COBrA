package com.aldodaquino.cobra.gui;

import java.io.*;
import java.net.*;
import java.util.Map;

public class HttpHelper {

    // --Commented out by Inspection (18/07/2018, 12:27):private static CookieManager cookieManager = new CookieManager();

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
            //connection.setRequestProperty("Cookie", joinCookie(cookieManager.getCookieStore().getCookies())); TODO: delete
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            //Send request
            if(!parameters.equals("")) {
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(parameters);
                outputStream.close();
            }

            // Get cookies TODO: delete
            /*String cookiesHeader = connection.getHeaderField("Set-Cookie");
            List<HttpCookie> cookies = HttpCookie.parse(cookiesHeader);
            cookies.forEach(cookie -> cookieManager.getCookieStore().add(null, cookie));*/

            //Get Response
            status = connection.getResponseCode();
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
            return status < 0 ? null : new Response(status, "");
        }
        finally {
            if (connection != null) connection.disconnect();
        }
    }

    /*private static String joinCookie(List<?> list) {
        if (list == null || list.size() == 0) return null;
        final StringBuilder stringBuilder = new StringBuilder(list.size() * 16);
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) stringBuilder.append(";");
            Object elem = list.get(i);
            if (elem != null) stringBuilder.append(elem);
        }
        return stringBuilder.toString();
    } TODO: delete*/

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