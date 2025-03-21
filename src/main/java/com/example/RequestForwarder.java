package com.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class RequestForwarder {

    private static final Logger logger = LoggerFactory.getLogger(RequestForwarder.class);
    private static List<String> backendServers;
    private static int serverPort;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        // Load configuration
        loadConfiguration();

        // Start the server
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/", new ForwardHandler());
        server.setExecutor(threadPool); // Use the thread pool for handling requests
        server.start();
        logger.info("Request forwarder started on port {}", serverPort);
    }

    private static void loadConfiguration() {
        try {
            Configurations configs = new Configurations();
            PropertiesConfiguration config = configs.properties("application.properties");

            // Read server port
            serverPort = config.getInt("server.port");

            // Read backend servers
            String backendServersStr = config.getString("backend.servers");
            backendServers = Arrays.asList(backendServersStr.split(","));

            logger.info("Loaded configuration: server.port={}, backend.servers={}", serverPort, backendServers);
        } catch (ConfigurationException e) {
            logger.error("Failed to load configuration: {}", e.getMessage());
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    static class ForwardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                // Generate a unique request ID for tracing
                String requestId = UUID.randomUUID().toString();
                String requestPath = exchange.getRequestURI().getPath();
                String queryParams = exchange.getRequestURI().getQuery();

                logger.info("[{}] Received GET request: path={}, queryParams={}", requestId, requestPath, queryParams);

                // Forward the request to all backend servers in parallel
                for (String backend : backendServers) {
                    String fullUrl = backend + (queryParams != null ? "?" + queryParams : "");
                    threadPool.submit(() -> forwardRequest(requestId, fullUrl));
                }

                // Respond to the client immediately
                String response = "GET request forwarded to all backend servers";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                // Handle unsupported methods
                String response = "Unsupported HTTP method: " + method;
                exchange.sendResponseHeaders(405, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        private void forwardRequest(String requestId, String url) {
        	
            // Configure timeouts
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000) // 5 seconds to establish a connection
                    .setSocketTimeout(5000)  // 5 seconds to wait for data
                    .build();

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build()) {
                HttpGet httpGet = new HttpGet(url);

                logger.info("[{}] Forwarding GET request to {}", requestId, url);

                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    // Log the response status and body
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    logger.info("[{}] Response from {}: status={}, body={}", requestId, url, response.getStatusLine().getStatusCode(), responseBody);
                }
            } catch (Exception e) {
                logger.error("[{}] Failed to forward GET request to {}: {}", requestId, url, e.getMessage());
            }
        }
    }
}