package ch.eth.infsec.services;

import ch.eth.infsec.IMoviesApplication;
import ch.eth.infsec.model.User;
import ch.eth.infsec.util.CAUtil;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.*;
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
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Integers;
import org.bouncycastle.x509.X509Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.security.cert.Extension;
import java.security.cert.X509Extension;
import java.util.Date;
import java.util.Properties;

@Service
public class PKIServiceImpl implements PKIService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    final String certStorePath = "imoviescertificicatestore.pem";
    final String certStorePassword = "imovies";

    @Autowired
    CAService caService;

    @Autowired
    CertificateService certificateService;

    @Override
    public String issueCertificate(User user) {
        CAService.Identity caIdentity = caService.getSigningIdentity();

        KeyPair clientKeyPair = CAUtil.generateKeyPair();
        X509Certificate clientCertificate = generateCertificate(user, clientKeyPair.getPublic(), caIdentity);

        // store the certificate
        try {
            certificateService.saveCertificate(clientCertificate);
        } catch (IOException | GeneralSecurityException e) {
            throw new PKIServiceException("Could not save client certificate", e);
        }

        return generatePKCS12(clientKeyPair, new Certificate[] { clientCertificate, caIdentity.getCertificate() });

    }

    @Override
    public boolean isValid(Certificate certificate) {
        return false;
    }

    @Override
    public int numberOfCertificates() {
        return certificateService.countCertificates();
    }

    @Override
    public int numberOfCRL() {
        return 0;
    }

    @Override
    public int currentSerialNumber() {
        return Integer.parseInt(certificateService.loadProperty("serialNumber", "1"));
    }



    /*private void storeCertificate(X509Certificate certificate) {
        CertStore.
        JcaCertStoreBuilder builder = new JcaCertStoreBuilder();
        builder

    }*/

    /**
     * Generate client certificate for client to download/use.
     * @param user client
     * @param userKey client crypto identity
     * @param caIdentity certificate authority that will sign
     * @return client certificate
     */
    private X509Certificate generateCertificate(
            User user, PublicKey userKey, CAService.Identity caIdentity) {

        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);

        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                authority(caIdentity.getCertificate()),
                new BigInteger(currentSerialNumber() + ""),
                startDate,
                endDate,
                subject(user),
                SubjectPublicKeyInfo.getInstance(userKey.getEncoded())
        );

        try {
            JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
            builder.addExtension(org.bouncycastle.asn1.x509.Extension.authorityKeyIdentifier, true, extensionUtils.createAuthorityKeyIdentifier(caIdentity.getCertificate()));
            builder.addExtension(org.bouncycastle.asn1.x509.Extension.subjectKeyIdentifier, true, extensionUtils.createSubjectKeyIdentifier(userKey));
            builder.addExtension(org.bouncycastle.asn1.x509.Extension.basicConstraints, true, new BasicConstraints(0).toASN1Primitive());

            X509CertificateHolder certificateHolder = builder.build(CAUtil.contentSigner(caIdentity.getKeyPair()));

            certificateService.getProperties().setProperty("serialNumber", (currentSerialNumber() + 1) + "");
            certificateService.saveProperties();

            return new JcaX509CertificateConverter().getCertificate(certificateHolder);
        } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new PKIServiceException("Could not convert certificate", e);
        }
    }

    /**
     * Users' certificate subject data.
     * @param user to base data on
     * @return subject data
     */
    private X500Name subject(User user) {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, user.getUid());
        nameBuilder.addRDN(BCStyle.EmailAddress, user.getEmail());
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "Personal");
        return nameBuilder.build();
    }

    /**
     * Derive signing authority from certificate.
     * @param certificate to base authority on
     * @return authority data
     */
    private X500Name authority(Certificate certificate) {
        try {
            return new JcaX509CertificateHolder((X509Certificate)certificate).getSubject();
        } catch (CertificateEncodingException e) {
            throw new PKIServiceException("Invalid certificate", e);
        }
    }

    /**
     * Generate and store PKCS12 keystore for users keyPair and certificate chain, to be downloaded by user.
     * @param keyPair user identity
     * @param certificates chain
     * @return path to stored pkcs12 file
     */
    private String generatePKCS12(KeyPair keyPair, Certificate[] certificates) {
        //
        try {

            KeyStore store = KeyStore.getInstance("PKCS12");

            store.load(null, null);
            store.setKeyEntry("Client key", keyPair.getPrivate(), "password".toCharArray(), certificates);

            String path = "id.p12";
            FileOutputStream fOut = new FileOutputStream(new File(path));
            store.store(fOut, "password".toCharArray());

            return path;
        } catch (KeyStoreException e) {
            throw new PKIServiceException("Invalid provider BouncyCastle!", e);
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new PKIServiceException("Could not load null into keystore!", e);
        }
    }




}
