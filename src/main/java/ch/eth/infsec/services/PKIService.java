package ch.eth.infsec.services;

import ch.eth.infsec.model.User;
import org.bouncycastle.jcajce.provider.keystore.PKCS12;

import java.security.cert.Certificate;

public interface PKIService {

    /**
     * Issues a new certificate for the given user.
     * @param user User object.
     * @return String path to p12 file
     */
    String issueCertificate(User user);

    /**
     * Determine whether the given certificate is valid, i.e. it has been issued by us and has not been revoked.
     * @param certificate User certificate
     * @return true for valid, false invalid
     */
    boolean isValid(Certificate certificate);

    /**
     * Get the number of issued certificates.
     * @return number of issued certificates
     */
    int numberOfCertificates();

    /**
     * Get the number of revoked certificates.
     * @return number of revoked certificates
     */
    int numberOfCRL();

    /**
     * Calculate the current certificate serial number for our CA (based on previous certificates).
     * @return serial number
     */
    int currentSerialNumber();

}
