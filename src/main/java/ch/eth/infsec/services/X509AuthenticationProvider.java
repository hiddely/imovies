package ch.eth.infsec.services;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

public class X509AuthenticationProvider implements AuthenticationProvider {



    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        System.out.println("Auth");
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        System.out.println("AAAH");
        return false;
    }
}
