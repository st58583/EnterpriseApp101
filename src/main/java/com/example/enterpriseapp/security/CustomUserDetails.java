package com.example.enterpriseapp.security;

import com.example.enterpriseapp.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> (GrantedAuthority) role::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // můžeš si pak rozšířit
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // můžeš si pak rozšířit
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // můžeš si pak rozšířit
    }

    @Override
    public boolean isEnabled() {
        return true; // můžeš si pak rozšířit
    }

    public String getEmail() {
        return user.getEmail();
    }

}
