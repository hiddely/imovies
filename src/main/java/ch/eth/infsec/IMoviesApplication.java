package ch.eth.infsec;

import ch.eth.infsec.services.CAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class IMoviesApplication {

	public static void main(String[] args) {
		SpringApplication.run(IMoviesApplication.class, args);
	}

	@Autowired
	CAService caService;

	@PostConstruct
	public void generateCA() {
		CAService.Identity identity = caService.getSigningIdentity();

		System.out.print(identity);
	}

}
