package ch.eth.infsec.services;

import ch.eth.infsec.model.User;
import org.bouncycastle.jcajce.provider.keystore.PKCS12;

public interface PKIService {

    /**
     * Issues a new certificate for the given user.
     * @param user User object.
     * @return PKCS#12 keystore object
     */
    public PKCS12 issueCertificate(User user);

}
