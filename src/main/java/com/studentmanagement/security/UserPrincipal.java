package com.studentmanagement.security;

import com.studentmanagement.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Адаптер между нашей сущностью User и Spring Security UserDetails.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
