package com.smartcampus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(JwtAuthFilter.class.getName());
    private static final String BEARER_PREFIX = "Bearer ";

    // Paths that don't require a token
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "auth/login",
            ""
    );

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        if (PUBLIC_PATHS.contains(path)) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank()) {
            LOGGER.warning("Missing Authorization header. Path: " + path);
            abortUnauthorised(requestContext, "Missing Authorization header.");
            return;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            LOGGER.warning("Authorization header is not Bearer. Path: " + path);
            abortUnauthorised(requestContext, "Authorization header must use Bearer scheme.");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            Claims claims = JwtUtil.validateToken(token);
            requestContext.setProperty("authenticatedUser", claims.getSubject());
        } catch (JwtException e) {
            LOGGER.warning("Invalid JWT on path: " + path + " | " + e.getMessage());
            abortUnauthorised(requestContext, "Token is invalid or has expired.");
        }
    }

    private static void abortUnauthorised(ContainerRequestContext ctx, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 401);
        body.put("error", "Unauthorised");
        body.put("message", message);

        ctx.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(body)
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }
}