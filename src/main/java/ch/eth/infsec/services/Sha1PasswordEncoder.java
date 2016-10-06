package ch.eth.infsec.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Sha1PasswordEncoder implements PasswordEncoder {

    public String encode(CharSequence rawPassword) {
        return DigestUtils.sha1Hex(rawPassword.toString());
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return DigestUtils.sha1Hex(rawPassword.toString()).equals(encodedPassword);
    }
}