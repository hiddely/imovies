package ch.eth.infsec.util;

import ch.eth.infsec.services.PKIServiceException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcECContentSignerBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;
import java.util.Date;

/**
 * Class to manage Certificate Authority operations. Generation of identity, load store certs/revocations
 */
public class CAUtil {

    final static String crlPath = "crypto/crl.pem";


    /**
     * Get a new, random keypair.
     * @return ECDSA 256 bits keypair
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new PKIServiceException("Invalid algorithm used to generate keypair!", e);
        }
    }


    /**
     * Generate a signer object with given algorithms (ECDSA keys, SHA1RSA at the moment).
     * @param keyPair identity
     * @return contentSigner object
     */
    public static ContentSigner contentSigner(KeyPair keyPair) {
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        try {
            AsymmetricKeyParameter privateKeyAsymKeyParam = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
            return new BcECContentSignerBuilder(sigAlgId, digAlgId).build(privateKeyAsymKeyParam);
        } catch (IOException | OperatorCreationException e) {
            throw new PKIServiceException("Invalid algorithm used to sign content!", e);
        }
    }

    /*
    public static KeyStore generateCA() {

    }

    public static boolean isRevoked(X509Certificate certificate) {
        // load CRL
        X509CRLHolder crlHolder = getCRLHolder();
        return crlHolder != null && crlHolder.getRevokedCertificate(certificate.getSerialNumber()).equals(certificate);
    }

    public static boolean revoke(X509Certificate certificate) {
        X509CRLHolder crlHolder = getCRLHolder();
            try {
                X509v2CRLBuilder builder = new X509v2CRLBuilder(
                        new JcaX509CertificateHolder(certificate).getIssuer(),
                        new Date());
                builder.b
            } catch (CertificateEncodingException e) {
                return false;
            }
        return true;
    }

    private static X509CRLHolder getCRLHolder() {
        Resource resource = new ClassPathResource("crypto/" + CAUtil.crlPath);
        X509CRLHolder crlHolder = null;
        try {
            InputStream inputStream = resource.getInputStream();
            if (inputStream != null) {
                crlHolder = new X509CRLHolder(inputStream);
            }
        } catch (IOException ignored) {
        }

        return crlHolder;
    }

    public static boolean saveCertificate(X509Certificate certificate) {
        certificate.
    }

    public static X509Certificate loadCertificate() {
        CertStore
    }
*/
}
