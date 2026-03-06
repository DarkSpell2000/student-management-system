package com.studentmanagement.model;

import io.micronaut.data.annotation.*;
import io.micronaut.security.authentication.Authentication;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 60, max = 60) // BCrypt hash length
    private String password;

    @NotBlank
    private String fullName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Version
    private Long version;

    @DateCreated
    private java.time.Instant dateCreated;

    @DateUpdated
    private java.time.Instant dateUpdated;

    // Constructors
    public User() {}

    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public void addRole(String role) { this.roles.add(role); }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public java.time.Instant getDateCreated() { return dateCreated; }
    public void setDateCreated(java.time.Instant dateCreated) { this.dateCreated = dateCreated; }

    public java.time.Instant getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(java.time.Instant dateUpdated) { this.dateUpdated = dateUpdated; }
}