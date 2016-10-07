package ch.eth.infsec.services;

import ch.eth.infsec.model.User;
import org.bouncycastle.jcajce.provider.keystore.PKCS12;

public interface PKIService {

    /**
     * Issues a new certificate for the given user.
     * @param user User object.
     * @return String path to p12 file
     */
    public String issueCertificate(User user);

}
