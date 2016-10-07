package ch.eth.infsec.services;

import ch.eth.infsec.IMoviesApplication;
import ch.eth.infsec.model.User;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jcajce.PKCS12Key;
import org.bouncycastle.jcajce.provider.keystore.PKCS12;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcECContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import sun.security.x509.X509CertImpl;


import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Date;

@Service
public class PKIServiceImpl implements PKIService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    final String caKeyStoreFile = "imovieskeystore.pfx";
    final String caKeyStorePassword = "imovies";
    final String caKeyStoreKeyPassword = "imovies";
    final String caKeyStoreAlias = "imovieskeystore";

    @Override
    public String issueCertificate(User user) {
        KeyStore caKeyStore = caKeyStore();
        Certificate caCertificate = null;
        KeyPair caKeyPair = null;
        try {
            Key privateKey = caKeyStore.getKey(caKeyStoreAlias, caKeyStoreKeyPassword.toCharArray());
            caCertificate = caKeyStore.getCertificate(caKeyStoreAlias);
            caKeyPair = new KeyPair(caCertificate.getPublicKey(), (PrivateKey)privateKey);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new RuntimeException("Could not load key from keyStore", e);
        }

        KeyPair clientKeyPair = generateKeyPair(user);
        Certificate clientCertificate = generateCertificate(user, clientKeyPair.getPublic(), caKeyPair, caCertificate);
        return generatePKCS12(clientKeyPair, new Certificate[] { clientCertificate, caCertificate });

        //return null;

    }

    private Certificate generateCertificate(
            User user, PublicKey userKey, KeyPair caDetails, Certificate caCert) {

        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);


        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                authority(caCert),
                BigInteger.ONE,
                startDate,
                endDate,
                subject(user),
                SubjectPublicKeyInfo.getInstance(userKey.getEncoded())
        );

        X509CertificateHolder certificateHolder = builder.build(contentSigner(caDetails));

        try {
            return new JcaX509CertificateConverter().getCertificate(certificateHolder);
        } catch (CertificateException e) {
            throw new RuntimeException("Could not convert certificate", e);
        }
    }

    private KeyPair generateKeyPair(User user) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid algorithm used to generate keypair!", e);
        }
    }

    private X500Name subject(User user) {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, user.getUid());
        nameBuilder.addRDN(BCStyle.EmailAddress, user.getEmail());
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "Personal");
        return nameBuilder.build();
    }

    /**
     * Generate the authority. Will load from file later.
     * @return authority data
     */
    private X500Name authority(Certificate certificate) {
        try {
            return new JcaX509CertificateHolder((X509Certificate)certificate).getSubject();
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("Invalid certificate", e);
        }

//        return new X500Name(((X509Certificate)certificate).getSubjectX500Principal().getName());

        /*
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "imovies.com");
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "CA");
        return nameBuilder.build();*/
    }

    private KeyStore caKeyStore() {
        // Load from keystore file
        try {
            Resource resource = new ClassPathResource("crypto/" + caKeyStoreFile);
            InputStream is = resource.getInputStream();

            logger.error("Inputstream: " + is.toString());

            KeyStore store = KeyStore.getInstance("PKCS12");
            store.load(is, caKeyStorePassword.toCharArray());

            logger.error("Store: ", store.aliases().nextElement());

            return store;
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new RuntimeException("Could not load CA keystore", e);
        }

    }

    private String generatePKCS12(KeyPair keyPair, Certificate[] certificates) {
        //
        try {

            KeyStore store = KeyStore.getInstance("PKCS12");

            X500Principal principalParent = ((X509Certificate)certificates[0]).getIssuerX500Principal();
            X500Principal principalChild = ((X509Certificate)certificates[1]).getSubjectX500Principal();

            store.load(null, null);
            store.setKeyEntry("Client key", keyPair.getPrivate(), "password".toCharArray(), certificates);

            String path = "id.p12";
            FileOutputStream fOut = new FileOutputStream(new File(path));
            store.store(fOut, "password".toCharArray());

            return path;
        } catch (KeyStoreException e) {
            throw new RuntimeException("Invalid provider BouncyCastle!", e);
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Could not load null into keystore!", e);
        }

    }

    private ContentSigner contentSigner(KeyPair keyPair) {
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        try {
            AsymmetricKeyParameter privateKeyAsymKeyParam = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
            return new BcECContentSignerBuilder(sigAlgId, digAlgId).build(privateKeyAsymKeyParam);
        } catch (IOException | OperatorCreationException e) {
            throw new RuntimeException("Invalid algorithm used to sign content!", e);
        }
    }

}
