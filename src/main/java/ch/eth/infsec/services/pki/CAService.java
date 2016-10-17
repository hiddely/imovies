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
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

@Service
public class CAService {

    private final String caKeyStoreFile = "imoviesca.pfx";
    private final String caKeyStorePassword = "imovies";
    private final String caKeyStoreRootKeyPassword = "imovies";
    private final String caKeyStoreRootAlias = "imovieskeystoreroot";
    private final String caKeyStoreIntermediateKeyPassword = "imovies";
    private final String caKeyStoreIntermediateAlias = "imovieskeystoreintermediate";

    public Identity getSigningIdentity() {

        KeyStore caKeyStore = loadKeystore();

        if (caKeyStore == null) {
            // generate new
            caKeyStore = generateCA();
        }

        try {
            Key privateKey = caKeyStore.getKey(caKeyStoreIntermediateAlias, caKeyStoreIntermediateKeyPassword.toCharArray());
            X509Certificate certificate = (X509Certificate) caKeyStore.getCertificate(caKeyStoreIntermediateAlias);
            KeyPair caKeyPair = new KeyPair(certificate.getPublicKey(), (PrivateKey)privateKey);

            return new Identity(caKeyPair, certificate);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new PKIServiceException("Could extract intermediate from keystore", e);
        }
    }

    /**
     * Get the CA identity from file
     * @return CA identity in keystore format.
     */
    private KeyStore loadKeystore() {
        try {
            File file = new File(CAUtil.cryptoPath + "/" + caKeyStoreFile);
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

            String path = CAUtil.cryptoPath + "/" + caKeyStoreFile;
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
        nameBuilder.addRDN(BCStyle.CN, "root.imovies.com");
        nameBuilder.addRDN(BCStyle.EmailAddress, "admin@imovies.com");
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "Root");
        return nameBuilder.build();
    }

    private X500Name intermediateName() {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "intermediate.imovies.com");
        nameBuilder.addRDN(BCStyle.EmailAddress, "admin@imovies.com");
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "Intermediate");
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
