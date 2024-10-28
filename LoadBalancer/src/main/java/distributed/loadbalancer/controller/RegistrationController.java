package distributed.loadbalancer.controller;

import distributed.loadbalancer.repository.Servers.Server;
import distributed.loadbalancer.repository.dto.RegistDto;
import distributed.loadbalancer.service.RegistrationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/loadbalancer")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    // 서버 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerServer(HttpServletRequest request,
                                                 @RequestBody RegistDto registDto) {
        String ip = request.getRemoteAddr();  // IP 추출
        log.info("Server registration request received from IP: {}", ip);

        boolean result = registrationService.addServer(registDto, ip);  // IP 전달

        if (result) {
            log.info("Server registered successfully: IP={}, Port={}, Protocol={}",
                    ip, registDto.getPort(), registDto.getProtocol());
            return ResponseEntity.ok("Server registered successfully.");
        } else {
            log.warn("Server registration failed: IP={}, Port={}, Protocol={}",
                    ip, registDto.getPort(), registDto.getProtocol());
            return ResponseEntity.status(400).body("Server already registered.");
        }
    }


    // 서버 등록 해제
    @PostMapping("/unregister")
    public ResponseEntity<String> unregisterServer(HttpServletRequest request,
                                                   @RequestBody RegistDto registDto) {
        String ip = request.getRemoteAddr();  // IP 추출
        log.info("Server unregistration request received from IP: {}", ip);

        boolean result = registrationService.removeServer(registDto, ip);  // IP 전달

        if (result) {
            log.info("Server unregistered successfully: IP={}, Port={}, Protocol={}",
                    ip, registDto.getPort(), registDto.getProtocol());
            return ResponseEntity.ok("Server unregistered successfully.");
        } else {
            log.warn("Server unregistration failed: IP={}, Port={}, Protocol={}",
                    ip, registDto.getPort(), registDto.getProtocol());
            return ResponseEntity.status(404).body("Server not found.");
        }
    }

    // 등록된 서버 목록 조회
    @GetMapping("/servers")
    public Set<Server> getAllServers() {
        return registrationService.getAllServers();
    }
}
