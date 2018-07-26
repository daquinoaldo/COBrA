package com.aldodaquino.cobra.connections;

import com.aldodaquino.javautils.HttpHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.function.BiConsumer;

/**
 * Contains method that help to make http request.
 * Works with JSON body for POST request and query-style GET parameters.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class CobraHttpHelper extends HttpHelper {

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

}