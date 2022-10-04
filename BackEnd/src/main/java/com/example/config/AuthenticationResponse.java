package com.example.config;

import java.util.Collection;

public class AuthenticationResponse {
    private final String jwt;
    private final String username;
    private final Collection<?> roles;

    public String getUsername() {
        return username;
    }

    public Collection<?> getRoles() {
        return roles;
    }

    public AuthenticationResponse(String jwt, String username, Collection<?> roles){
        this.jwt = jwt;
        this.username = username;
        this.roles = roles;
    }

    public String getJwt(){
        return jwt;
    }
}
