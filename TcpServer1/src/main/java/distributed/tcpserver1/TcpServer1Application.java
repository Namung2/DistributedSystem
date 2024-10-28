package distributed.tcpserver1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TcpServer1Application {

	public static void main(String[] args) {
		SpringApplication.run(TcpServer1Application.class, args);
		TcpServer1 tcpServer = new TcpServer1("localhost", 8080);
		tcpServer.startConsole();
	}

}
