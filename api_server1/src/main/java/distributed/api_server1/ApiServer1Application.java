package distributed.api_server1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiServer1Application {
	public static void main(String[] args) {
		SpringApplication.run(ApiServer1Application.class, args);
		HttpServerController.startConsole();
	}
}