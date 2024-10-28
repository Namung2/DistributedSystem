package distributed.loadbalancer.service;

import distributed.loadbalancer.repository.Servers.Server;
import distributed.loadbalancer.repository.Servers.ServerRepository;
import distributed.loadbalancer.healthcheck.service.HealthCheckService;
import distributed.loadbalancer.repository.dto.RegistDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class RegistrationService {

    private final ServerRepository serverRepository = ServerRepository.getInstance();
    private final HealthCheckService healthCheckService;

    @Autowired
    public RegistrationService(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    // 1. HTTP 요청을 통한 서버 등록
    public boolean addServer(RegistDto registDto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return addServerInternal(ip, registDto);
    }

    // 2. TCP/UDP 요청을 통한 서버 등록 (IP와 포트를 직접 전달)
    public boolean addServer(RegistDto registDto, String ip) {
        return addServerInternal(ip, registDto);
    }

    // 공통 서버 등록 로직 (중복 확인 및 예외 처리 포함)
    private boolean addServerInternal(String ip, RegistDto registDto) {
        Server newServer = new Server(ip, registDto.getPort(), registDto.getProtocol());

        if (serverRepository.checkAllServers().contains(newServer)) {
            log.warn("Server already registered: {}", newServer);
            return false;
        }

        try {
            boolean added = serverRepository.addServer(newServer);
            if (added) {
                log.info("Server registered successfully: {}", newServer);
                triggerHealthCheck();
                return true;
            } else {
                log.warn("Failed to register server (repository failure): {}", newServer);
                return false;
            }
        } catch (Exception e) {
            log.error("Exception while registering server: {}", newServer, e);
            return false;
        }
    }

    // 3. HTTP 요청을 통한 서버 해제
    public boolean removeServer(RegistDto registDto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return removeServerInternal(ip, registDto);
    }

    // 4. TCP/UDP 요청을 통한 서버 해제 (IP와 포트를 직접 전달)
    public boolean removeServer(RegistDto registDto, String ip) {
        return removeServerInternal(ip, registDto);
    }

    // 공통 서버 해제 로직 (예외 처리 및 로그 개선)
    private boolean removeServerInternal(String ip, RegistDto registDto) {
        Server server = new Server(ip, registDto.getPort(), registDto.getProtocol());

        try {
            boolean removed = serverRepository.removeServer(server);
            if (removed) {
                log.info("Server unregistered successfully: {}", server);
                return true;
            } else {
                log.warn("Failed to unregister server: {}", server);
                return false;
            }
        } catch (Exception e) {
            log.error("Exception while unregistering server: {}", server, e);
            return false;
        }
    }

    // 등록된 서버 목록 반환
    public Set<Server> getAllServers() {
        return serverRepository.checkAllServers();
    }

    // 헬스 체크 트리거 (성능 최적화)
    private void triggerHealthCheck() {
        if (serverRepository.checkAllServers().isEmpty()) {
            log.warn("No servers available for health check.");
            return;
        }

        log.info("Triggering health check.");
        try {
            healthCheckService.performHealthCheck();
        } catch (Exception e) {
            log.error("Health check failed", e);
        }
    }
}
