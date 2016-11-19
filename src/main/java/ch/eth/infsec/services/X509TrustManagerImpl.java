package ch.eth.infsec.services;

import ch.eth.infsec.IMoviesApplication;
import ch.eth.infsec.services.pki.CAService;
import ch.eth.infsec.services.pki.CertificateStoreService;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.net.ssl.X509TrustManager;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Configurable
public class X509TrustManagerImpl implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        X509CertSelector target = new X509CertSelector();
        target.setCertificate(x509Certificates[0]);
        try {
            PKIXBuilderParameters params = new PKIXBuilderParameters(CertificateStoreService.trustStore, target);
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            CertPathBuilderResult result = builder.build(params);

            if (CertificateStoreService.crl != null) {
                X509CRL crl = new JcaX509CRLConverter().getCRL(CertificateStoreService.crl);
                if (crl.getRevokedCertificate(x509Certificates[0]) != null) {
                    throw new CertificateException("Certificate was revoked");
                }
            }

        } catch (KeyStoreException | InvalidAlgorithmParameterException | CRLException | NoSuchAlgorithmException | CertPathBuilderException e) {
            throw new CertificateException("Client not trusted", e);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        //System.out.println("checkservetrusted");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] { IMoviesApplication.intermediateCertificate };
    }

}
