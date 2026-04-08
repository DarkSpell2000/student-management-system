package com.studentmanagement.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Set;

@Serdeable
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private Set<String> roles;
    private Long userId;
    private String fullName;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String username, Set<String> roles, Long userId, String fullName) {
        this.accessToken = accessToken;
        this.username = username;
        this.roles = roles;
        this.userId = userId;
        this.fullName = fullName;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
