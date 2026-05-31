package com.smartcampus.resource;

import com.smartcampus.security.JwtUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/auth")
public class AuthResource {

    // Read from env vars; fall back to dev defaults
    private static final String VALID_USERNAME = getEnvOrDefault("ADMIN_USERNAME", "admin");
    private static final String VALID_PASSWORD = getEnvOrDefault("ADMIN_PASSWORD", "password123");

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return badCredentials();
        }

        boolean valid = VALID_USERNAME.equals(request.getUsername().trim())
                && VALID_PASSWORD.equals(request.getPassword());

        if (!valid) {
            return badCredentials();
        }

        Map<String, String> body = new LinkedHashMap<>();
        body.put("token", JwtUtil.generateToken(request.getUsername().trim()));
        return Response.ok(body).build();
    }

    private static Response badCredentials() {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 401);
        error.put("error", "Unauthorised");
        error.put("message", "Invalid username or password.");
        return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}