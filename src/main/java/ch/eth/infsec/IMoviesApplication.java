package ch.eth.infsec;

import ch.eth.infsec.services.pki.CAService;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.Security;

@SpringBootApplication
public class IMoviesApplication {

	public static void main(String[] args) {
		SpringApplication.run(IMoviesApplication.class, args);
	}

	@Autowired
	CAService caService;

	@PostConstruct
	public void addBCProvider() {
		Security.addProvider(new BouncyCastleProvider());
		Security.setProperty("ocsp.enable", "true");
	}

	@Bean
	public EmbeddedServletContainerCustomizer customizer() {
		return new EmbeddedServletContainerCustomizer() {

			@Override
			public void customize(ConfigurableEmbeddedServletContainer configurableEmbeddedServletContainer) {
				TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) configurableEmbeddedServletContainer;
				//this will only handle the case where SSL is enabled on the main tomcat connector
				tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {
					@Override
					public void customize(Connector connector) {
						Http11NioProtocol handler = (Http11NioProtocol) connector.getProtocolHandler();
						File crlFile = new File("src/main/resources/crypto/revoked.crl");
						File caFile = new File("src/main/resources/crypto/imoviesca.pfx");
						File trustFile = new File("src/main/resources/crypto/trust.jks");
						if (crlFile.exists()) {
							/*SSLHostConfig config = new SSLHostConfig();
							config.setCertificateKeystoreFile("serverIdentity.p12");
							config.setCertificateKeystorePassword("imovies");
							config.setCertificateKeystoreType("PKCS12");
							config.setCertificateKeyAlias("imovies.com");
							config.*/

							//SSLHostConfig config = connector.findSslHostConfigs()[0];

							//config.setCertificateRevocationListFile(crlFile.getAbsolutePath());
							//config.setTruststoreFile(trustFile.getAbsolutePath());
							//config.setTruststorePassword("imoviestruststore");
							//config.setTruststoreProvider("JKS");
							//config.setHostName("imovies.com");

							//connector.addSslHostConfig(config);
							handler.setCrlFile(crlFile.getAbsolutePath());

							//connector.
							System.out.println("Hi");
						}
					}
				});
			}
		};
	}

	//@PostConstruct
	public void generateCA() {


		System.out.print(Security.getProviders());
		//CAService.Identity identity = caService.getSigningIdentity();

		//System.out.print(identity);
	}

}
