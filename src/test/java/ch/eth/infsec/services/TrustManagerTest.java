package ch.eth.infsec.services;

import ch.eth.infsec.model.User;
import ch.eth.infsec.services.pki.CAService;
import ch.eth.infsec.services.pki.CertificateStoreService;
import ch.eth.infsec.services.pki.PKIService;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.*;

import static java.lang.System.out;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TrustManagerTest {

    @Autowired
    PKIService pkiService;

    @Autowired
    CertificateStoreService certificateStoreService;

    User user = new User();

    @Before
    public void setUp() throws Exception {
        user.setUid("ABC");
        user.setEmail("hello@gmail.com");
        user.setPwd("pwd");
        user.setFirstname("John");
        user.setLastname("Appleseed");
    }

    @Test
    public void testCertificateValid() throws KeyStoreException, CertificateException {
        pkiService.issueCertificate(user, "password", null);

        X509Certificate certificate = certificateStoreService.getCertificate(user.getUid());
        verifyCert(certificate);
    }

    @Test(expected = CertificateException.class)
    public void testCertificateInvalid() throws KeyStoreException, CertificateException {
        pkiService.issueCertificate(user, "password", null);

        X509Certificate certificate = certificateStoreService.getCertificate(user.getUid());

        pkiService.revokeCertificate(user);
        verifyCert(certificate);
    }

    private X509CRL getCRL() throws CRLException {
        return new JcaX509CRLConverter().getCRL(CertificateStoreService.crl);
    }

    private void verifyCert(X509Certificate targetCert) throws CertificateException {
        try {
            X509CertSelector target = new X509CertSelector();
            target.setCertificate(targetCert);

            CAService.Identity rootId = CAService.caIdentityChain[0];
            //anchorStore.setCertificateEntry("root", rootId.getCertificate());

            KeyStore caKeystore = CAService.loadKeystore();
            X509Certificate rootCert = (X509Certificate)caKeystore.getCertificate(CAService.caKeyStoreRootAlias);
            X509Certificate interCert = (X509Certificate)caKeystore.getCertificate(CAService.caKeyStoreIntermediateAlias);

            Set<TrustAnchor> hashSet = new HashSet<TrustAnchor>();
            hashSet.add(new TrustAnchor(rootCert, null));
            hashSet.add(new TrustAnchor(interCert, null));

            //printCert(rootCert);
            //printCert(interCert);
            //printCert(targetCert);

            List<X509Certificate> interList = new ArrayList<>();
            interList.add(rootCert);
            interList.add(interCert);

            PKIXBuilderParameters params = new PKIXBuilderParameters(CertificateStoreService.trustStore, target);
            CertStoreParameters intermediates = new CollectionCertStoreParameters(interList);
            params.addCertStore(CertStore.getInstance("Collection", intermediates));
            CertStoreParameters revoked = new CollectionCertStoreParameters(Collections.singletonList(getCRL()));
            params.addCertStore(CertStore.getInstance("Collection", revoked));
            params.setRevocationEnabled(true);
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            CertPathBuilderResult result = builder.build(params);

            X509CRL crl = new JcaX509CRLConverter().getCRL(CertificateStoreService.crl);
            if (crl.getRevokedCertificate(targetCert) != null) {
                throw new CertificateException("Revoked certificate.");
            }
        } catch (KeyStoreException | InvalidAlgorithmParameterException | CRLException | NoSuchAlgorithmException | CertPathBuilderException e) {
            e.printStackTrace();
            throw new CertificateException("Client not trusted", e);
        }
    }

    private void printCert(X509Certificate certificate) throws CertificateEncodingException, IOException {
        BASE64Encoder encoder = new BASE64Encoder();
        out.println(X509Factory.BEGIN_CERT);
        encoder.encodeBuffer(certificate.getEncoded(), out);
        out.println(X509Factory.END_CERT);
    }

}
