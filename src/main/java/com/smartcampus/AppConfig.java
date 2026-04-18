package com.smartcampus;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class AppConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<>();
    }
}