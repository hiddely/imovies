package ch.eth.infsec.services.pki;

import ch.eth.infsec.util.CAUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

@Service
public class CAService {

    private final static String caKeyStoreFile = "imoviesca.pfx";
    private final static String caKeyStorePassword = "imovies";
    private final String caKeyStoreRootKeyPassword = "imovies";
    public static final String caKeyStoreRootAlias = "imovieskeystoreroot";
    private final String caKeyStoreIntermediateKeyPassword = "imovies";
    public static final String caKeyStoreIntermediateAlias = "imovieskeystoreintermediate";

    @Autowired
    PKIService pkiService;

    public static Identity[] caIdentityChain;

    public Identity getSigningIdentity() {

        KeyStore caKeyStore = loadKeystore();

        try {

            if (caKeyStore == null) {
                // generate new
                caKeyStore = generateCA();

                Identity identity = buildCAChain(caKeyStore);
                for (int i = 0; i < 3; i++) {
                    pkiService.issueCertificate(null, "password", identity);
                }

                return identity;

            }

            return buildCAChain(caKeyStore);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new PKIServiceException("Could extract intermediate from keystore", e);
        }
    }

    /**
     * Builds the CA chain, stores in static var. Returns intermediate identity.
     * @param caKeyStore
     * @return
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    private Identity buildCAChain(KeyStore caKeyStore) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        Key iPrivateKey = caKeyStore.getKey(caKeyStoreIntermediateAlias, caKeyStoreIntermediateKeyPassword.toCharArray());
        X509Certificate iCertificate = (X509Certificate) caKeyStore.getCertificate(caKeyStoreIntermediateAlias);
        KeyPair iCaKeyPair = new KeyPair(iCertificate.getPublicKey(), (PrivateKey)iPrivateKey);
        Identity iIdentity = new Identity(iCaKeyPair, iCertificate);

        Key rPrivateKey = caKeyStore.getKey(caKeyStoreRootAlias, caKeyStoreRootKeyPassword.toCharArray());
        X509Certificate rCertificate = (X509Certificate) caKeyStore.getCertificate(caKeyStoreRootAlias);
        KeyPair rCaKeyPair = new KeyPair(rCertificate.getPublicKey(), (PrivateKey)rPrivateKey);
        Identity rIdentity = new Identity(rCaKeyPair, rCertificate);

        CAService.caIdentityChain = new Identity[] {
            rIdentity, iIdentity
        };

        return iIdentity;
    }

    /**
     * Get the CA identity from file
     * @return CA identity in keystore format.
     */
    public static KeyStore loadKeystore() {
        try {
            File file = new File(CAUtil.cryptoPath + caKeyStoreFile);
            if (!file.exists()) {
                return null;
            }
            InputStream is = new FileInputStream(file);

            KeyStore store = KeyStore.getInstance("PKCS12");
            store.load(is, caKeyStorePassword.toCharArray());

            return store;
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new PKIServiceException("Could not load CA keystore", e);
        }
    }

    private KeyStore generateCA() {
        KeyPair rootKeyPair = CAUtil.generateKeyPair();

        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);

        X500Name root_cn = rootName();

        X509v3CertificateBuilder rootBuilder = new X509v3CertificateBuilder(
                root_cn,
                BigInteger.ONE,
                startDate,
                endDate,
                root_cn,
                SubjectPublicKeyInfo.getInstance(rootKeyPair.getPublic().getEncoded())
        );

        try {
            JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
            rootBuilder.addExtension(Extension.subjectKeyIdentifier, true, extensionUtils.createSubjectKeyIdentifier(rootKeyPair.getPublic()));
            rootBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(1).toASN1Primitive());

            X509CertificateHolder rootCertificateHolder = rootBuilder.build(CAUtil.contentSigner(rootKeyPair)); // self-sign
            X509Certificate rootCertificate = new JcaX509CertificateConverter().getCertificate(rootCertificateHolder);

            KeyPair intermediateKeyPair = CAUtil.generateKeyPair();

            X509v3CertificateBuilder intermediateBuilder = new X509v3CertificateBuilder(
                    root_cn,
                    BigInteger.ONE,
                    startDate,
                    endDate,
                    intermediateName(),
                    SubjectPublicKeyInfo.getInstance(rootKeyPair.getPublic().getEncoded())
            );

            intermediateBuilder.addExtension(Extension.authorityKeyIdentifier, true, extensionUtils.createAuthorityKeyIdentifier(rootCertificate));
            intermediateBuilder.addExtension(Extension.subjectKeyIdentifier, true, extensionUtils.createSubjectKeyIdentifier(intermediateKeyPair.getPublic()));
            intermediateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(0).toASN1Primitive());

            X509CertificateHolder intermediateCertificateHolder = intermediateBuilder.build(CAUtil.contentSigner(intermediateKeyPair)); // self-sign
            X509Certificate intermediateCertificate = new JcaX509CertificateConverter().getCertificate(intermediateCertificateHolder);


            // Turn into PKCS12

            KeyStore store = KeyStore.getInstance("PKCS12");

            X509Certificate[] rootChain = new X509Certificate[] { rootCertificate };
            X509Certificate[] intermediateChain = new X509Certificate[] { intermediateCertificate, rootCertificate };

            store.load(null, null);
            store.setKeyEntry(caKeyStoreRootAlias, rootKeyPair.getPrivate(), caKeyStoreRootKeyPassword.toCharArray(), rootChain);
            store.setKeyEntry(caKeyStoreIntermediateAlias, intermediateKeyPair.getPrivate(), caKeyStoreIntermediateKeyPassword.toCharArray(), intermediateChain);

            String path = CAUtil.cryptoPath + caKeyStoreFile;
            File file = new File(path);
            file.createNewFile();

            FileOutputStream fOut = new FileOutputStream(file);
            store.store(fOut, caKeyStorePassword.toCharArray());

            return store;

        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new PKIServiceException("Could not generate CA", e);
        }

    }

    private X500Name rootName() {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "127.0.0.1");
        nameBuilder.addRDN(BCStyle.EmailAddress, "admin@imovies.com");
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "Root");
        return nameBuilder.build();
    }

    private X500Name intermediateName() {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "127.0.0.1");
        nameBuilder.addRDN(BCStyle.EmailAddress, "admin@imovies.com");
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "Intermediate");
        return nameBuilder.build();
    }

    private X500Name adminName() {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "Admin");
        return nameBuilder.build();
    }

    /**
     * Represents crypto identity, typically CA root or intermediate.
     */
    @AllArgsConstructor
    public static class Identity {

        @Getter @Setter
        private KeyPair keyPair;
        @Getter @Setter
        private X509Certificate certificate;

    }

}
