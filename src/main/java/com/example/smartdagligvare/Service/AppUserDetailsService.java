package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.DAO.AppUserRepository;
import com.example.smartdagligvare.Model.AppUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(
            AppUserRepository appUserRepository
    ) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException(
                    "Invalid username or password."
            );
        }

        AppUser user = appUserRepository
                .findByUsername(username.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid username or password."
                        )
                );

        // Existing profiles without credentials cannot log in.
        if (
                user.getPasswordHash() == null ||
                        user.getPasswordHash().isBlank()
        ) {
            throw new UsernameNotFoundException(
                    "Invalid username or password."
            );
        }

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}