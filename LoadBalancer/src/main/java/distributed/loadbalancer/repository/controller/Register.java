package distributed.loadbalancer.repository.controller;

import distributed.loadbalancer.repository.dto.RegistDto;
import distributed.loadbalancer.repository.Servers.Server;
import distributed.loadbalancer.repository.Servers.ServerRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/register")
public class Register {

    private final ServerRepository serverRepository = ServerRepository.getInstance();

    @PostMapping
    public ResponseEntity<String> handleRegistration(@RequestBody RegistDto registDto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        log.info("Received request: {} from IP: {}", registDto.getCmd(), ip);

        if ("register".equalsIgnoreCase(registDto.getCmd())) {
            Server server = new Server(ip, registDto.getPort(), registDto.getProtocol());
            serverRepository.addServer(server);
            log.info("Server registered: {}", server);
            return ResponseEntity.ok("{\"ack\":\"hello\"}");
        } else if ("unregister".equalsIgnoreCase(registDto.getCmd())) {
            Server server = new Server(ip, registDto.getPort(), registDto.getProtocol());
            serverRepository.removeServer(server);
            log.info("Server unregistered: {}", server);
            return ResponseEntity.ok("{\"ack\":\"goodbye\"}");
        } else {
            log.warn("Invalid command received: {}", registDto.getCmd());
            return ResponseEntity.badRequest().body("{\"error\":\"Invalid command\"}");
        }
    }
}
