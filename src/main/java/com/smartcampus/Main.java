package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.Scanner;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static void main(String[] args) {
        ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus.resource", "com.smartcampus.mapper");

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        System.out.println("Smart Campus API is running at " + BASE_URI);
        System.out.println("Press Enter to stop the server...");

        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }

        server.shutdownNow();
    }
}