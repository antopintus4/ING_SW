package com.afam.identity.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "utente_afam")
public class UtenteAfam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", nullable = false)
    private java.util.UUID uuid;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "has_2fa")
    private Boolean has2fa = false;

    @Column(name = "metodo_2fa", length = 50)
    private String metodo2fa;

    @Column(name = "registration_with_identity_provider")
    private Boolean registrationWithIdentityProvider = false;

    @Column(name = "identity_provider", length = 50)
    private String identityProvider;

    public UtenteAfam() {
    }

    // Metodi UML (segnaposto per aderenza 100% al RAD)
    public void registration() {}
    public void login() {}
    public void logout() {}
    public void validateCredential() {}
    public void generateAccessToken() {}
    public void modifyUserCredential() {}
    public void recoveryPassword() {}
    public void recoveryEmail() {}

    public java.util.UUID getUuid() {
        return uuid;
    }

    public void setUuid(java.util.UUID uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getHas2fa() {
        return has2fa;
    }

    public void setHas2fa(Boolean has2fa) {
        this.has2fa = has2fa;
    }

    public String getMetodo2fa() {
        return metodo2fa;
    }

    public void setMetodo2fa(String metodo2fa) {
        this.metodo2fa = metodo2fa;
    }

    public Boolean getRegistrationWithIdentityProvider() {
        return registrationWithIdentityProvider;
    }

    public void setRegistrationWithIdentityProvider(Boolean registrationWithIdentityProvider) {
        this.registrationWithIdentityProvider = registrationWithIdentityProvider;
    }

    public String getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider;
    }
}
