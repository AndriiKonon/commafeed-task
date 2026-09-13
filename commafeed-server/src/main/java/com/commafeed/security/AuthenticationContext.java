package com.commafeed.security;

import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;

import io.quarkus.security.identity.SecurityIdentity;

import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class AuthenticationContext {

    private final SecurityIdentity securityIdentity;
    private final UserDAO userDAO;

    public User getCurrentUser() {
        if (securityIdentity.isAnonymous()) {
            return null;
        }

        String principalName = securityIdentity.getPrincipal().getName();
        if (principalName == null) {
            return null;
        }

        try {
            return userDAO.findById(Long.valueOf(principalName));
        } catch (NumberFormatException e) {
            return userDAO.findByName(principalName);
        }
    }
}
