package com.afam.identity.service;

import com.afam.identity.entity.UtenteAfam;
import com.afam.identity.boundary.UtenteAfamDBMSBoundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtenteAfamDBMSBoundary utenteAfamDBMSBoundary;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UtenteAfam> utenteOpt = utenteAfamDBMSBoundary.findByUsername(username);
        if (utenteOpt.isEmpty()) {
            throw new UsernameNotFoundException("Utente non trovato");
        }
        
        UtenteAfam utente = utenteOpt.get();
        return new User(utente.getUsername(), utente.getPassword(), new ArrayList<>());
    }
}
