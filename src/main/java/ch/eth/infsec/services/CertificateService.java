package ch.eth.infsec.services;

import ch.eth.infsec.util.CAUtil;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.*;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.X509CRLParser;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

@Service
public class CertificateService {

    KeyStore trustStore;
    File trustStoreFile = new File(CAUtil.cryptoPath + "/trust.jks");
    final String trustStorePassword = "imoviestruststore";

    X509CRLHolder crl;
    File crlFile = new File(CAUtil.cryptoPath + "/revoked.crl");

    public CertificateService() throws GeneralSecurityException, IOException {

        trustStore = KeyStore.getInstance("JKS");
        trustStoreFile.createNewFile();
        if (trustStoreFile.length() > 0) {
            trustStore.load(new FileInputStream(trustStoreFile), trustStorePassword.toCharArray());
        } else {
            trustStore.load(null, null);
        }

    }

    public void revokeCertificate(X500Name caName, CAService.Identity caIdentity, BigInteger serialNumber, X509CRL existingCrl) throws NoSuchAlgorithmException, CertificateEncodingException, IOException {
        Date now = new Date();
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(caName, now);
        crlBuilder.setNextUpdate(new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000));

        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        crlBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.authorityKeyIdentifier, false, extensionUtils.createAuthorityKeyIdentifier(caIdentity.getCertificate()));
        crlBuilder.addExtension(Extension.cRLNumber, false,
                new CRLNumber(new BigInteger(
                        loadProperty("crlNumber", "1")
                ))
        );

        incrementProperty("crlNumber", "1");

        if (existingCrl != null) {
            crlBuilder.addCRL(crl);
        }

        crlBuilder.addCRLEntry(serialNumber, now, CRLReason.privilegeWithdrawn);

        crl = crlBuilder.build(CAUtil.contentSigner(caIdentity.getKeyPair()));

        saveCrl();
    }

    public void saveCertificate(X509Certificate certificate) throws IOException, GeneralSecurityException {
        trustStore.setCertificateEntry(certificate.getSerialNumber().toString(), certificate);
        FileOutputStream fOut = new FileOutputStream(trustStoreFile);
        trustStore.store(fOut, trustStorePassword.toCharArray());
    }

    private void saveCrl() throws IOException {
        FileWriter fileWriter = new FileWriter(crlFile);
        PemWriter pemWriter = new PemWriter(fileWriter);

        PemObjectGenerator generator = () -> {
            try {
                return new PemObject("CRL", crl.getEncoded());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        };

        pemWriter.writeObject(generator);
        pemWriter.flush();
        pemWriter.close();
    }

    public int countCertificates() throws KeyStoreException {
        return trustStore.size();
    }

    public void incrementProperty(String name, String def) {
        getProperties().setProperty(name, (Integer.parseInt(loadProperty(name, def)) + 1) + "");
    }

    public String loadProperty(String name, String def) {
        return getProperties().getProperty(name, def);
    }

    private Properties pkiProperties = null;

    public Properties getProperties() {
        if (pkiProperties != null) {
            return pkiProperties;
        }
        File configFile = new File(CAUtil.cryptoPath + "/config.properties");

        try {
            configFile.createNewFile();

            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);

            reader.close();

            pkiProperties = props;

            return props;
        } catch (IOException ex) {
            throw new PKIServiceException("Could not load property file", ex);
        }
    }

    public void saveProperties() {
        if (pkiProperties != null) {
            File configFile = new File(CAUtil.cryptoPath + "/config.properties");
            try {
                FileWriter writer = new FileWriter(configFile);
                pkiProperties.store(writer, "ca settings");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
