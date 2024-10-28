package distributed.loadbalancer.healthcheck.service;

import distributed.loadbalancer.repository.Servers.Server;
import distributed.loadbalancer.repository.Servers.ServerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class HealthCheckService {

    private final ServerRepository serverRepository = ServerRepository.getInstance();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 3000)
    public void performHealthCheck() {
        Set<Server> servers = serverRepository.checkAllServers();

        if (servers.isEmpty()) {
            log.info("No servers registered. Skipping health check.");
            return;  // 등록된 서버가 없으면 이번 주기는 건너뜁니다.
        }

        log.info("Performing health check on {} servers...", servers.size());

        for (Server server : servers) {
            executorService.submit(() -> {
                boolean isAlive = sendHealthCheck(server);
                if (!isAlive) {
                    log.warn("Server {}:{} (protocol: {}) did not respond with ACK. Removing it.",
                            server.getIp(), server.getPort(), server.getProtocol());
                    serverRepository.removeServer(server);
                }
            });
        }
    }


    private boolean sendHealthCheck(Server server) {
        try {
            String urlString = String.format("http://%s:%d/api/health", server.getIp(), server.getPort());
            log.info("Sending health check to: {}", urlString);

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);

            // Health check request: {"cmd": "hello"}
            String jsonRequest = objectMapper.writeValueAsString(Map.of("cmd", "hello"));
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonRequest.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            log.info("Received response code {} from server: {}:{}", responseCode, server.getIp(), server.getPort());

            if (responseCode == 200) {
                try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()))) {
                    String response = br.readLine();
                    log.info("Response from server {}:{} -> {}", server.getIp(), server.getPort(), response);
                    return response.contains("\"ack\":\"hello\"");
                }
            }
        } catch (Exception e) {
            log.error("Failed to send health check to {}:{}", server.getIp(), server.getPort(), e);
        }
        return false;
    }
}
