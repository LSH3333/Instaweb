package web.instaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;

@SpringBootApplication
@PropertySource(value = {"classpath:secret.yml"}, factory = FileConfig.class)
public class InstawebApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(InstawebApplication.class, args);
	}


}
