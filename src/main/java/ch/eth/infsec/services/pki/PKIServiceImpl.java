package ch.eth.infsec.services.pki;

import ch.eth.infsec.model.User;
import ch.eth.infsec.util.CAUtil;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Date;

@Service
public class PKIServiceImpl implements PKIService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

        return generatePKCS12(user, clientKeyPair, new Certificate[] { clientCertificate, caIdentity.getCertificate() });

    }

    @Override
    public boolean revokeCertificate(User user) {
        try {
            X509Certificate certificate = certificateService.getCertificate(user.getUid());
            if (certificate == null || !certificateService.hasCertificate(certificate)) {
                return false;
            }
            CAService.Identity caIdentity = caService.getSigningIdentity();

            certificateService.revokeCertificate(
                    new JcaX509CertificateHolder(caIdentity.getCertificate()).getSubject(),
                    caIdentity,
                    certificate
            );

            logger.info("Revoking certificate " + certificate.getSerialNumber().toString() + " for user UID " + user.getUid());

            return true;
        } catch (NoSuchAlgorithmException | CertificateEncodingException | KeyStoreException | IOException e) {
            throw new PKIServiceException("Could not revoke certificate", e);
        }
    }

    @Override
    public boolean isValid(Certificate certificate) {
        return false;
    }

    @Override
    public int numberOfCertificates() {
        try {
            return certificateService.countCertificates();
        } catch (KeyStoreException e) {
            throw new PKIServiceException("Could count certificates", e);
        }
    }

    @Override
    public int numberOfCRL() {
        return certificateService.countCRL();
    }

    @Override
    public int currentSerialNumber() {
        return Integer.parseInt(certificateService.loadProperty("serialNumber", "1"));
    }


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
            builder.addExtension(org.bouncycastle.asn1.x509.Extension.basicConstraints, true, new BasicConstraints(false).toASN1Primitive());

            ASN1EncodableVector purposes = new ASN1EncodableVector();
            purposes.add(KeyPurposeId.id_kp_clientAuth);
            purposes.add(KeyPurposeId.anyExtendedKeyUsage);
            builder.addExtension(org.bouncycastle.asn1.x509.Extension.extendedKeyUsage, false,
                    new DERSequence(purposes));

            builder.addExtension(MiscObjectIdentifiers.netscapeCertType,
                    false, new NetscapeCertType(NetscapeCertType.sslClient
                            | NetscapeCertType.smime));

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
        nameBuilder.addRDN(BCStyle.O, "iMovies");
        nameBuilder.addRDN(BCStyle.OU, "Personal");
        nameBuilder.addRDN(BCStyle.EmailAddress, user.getEmail());
        nameBuilder.addRDN(BCStyle.CN, user.getUid());
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
     * @param user employee who this is for
     * @param keyPair user identity
     * @param certificates chain
     * @return path to stored pkcs12 file
     */
    private String generatePKCS12(User user, KeyPair keyPair, Certificate[] certificates) {
        //
        try {

            KeyStore store = KeyStore.getInstance("PKCS12");

            store.load(null, null);
            store.setKeyEntry("Client key", keyPair.getPrivate(), "password".toCharArray(), certificates);

            String path = CAUtil.cryptoPath + "certificates/";
            String filename =  user.getUid() + "-" + System.currentTimeMillis() + ".p12";
            String fullFile = path + filename;
            File folder = new File(path);
            folder.mkdirs();
            File fileOutput = new File(fullFile);
            fileOutput.createNewFile();
            FileOutputStream fOut = new FileOutputStream(fileOutput);
            store.store(fOut, "password".toCharArray());

            logger.info("Generated client identity and saved at " + fullFile);

            return fullFile;
        } catch (KeyStoreException e) {
            throw new PKIServiceException("Invalid provider BouncyCastle!", e);
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new PKIServiceException("Could not load null into keystore!", e);
        }
    }




}
