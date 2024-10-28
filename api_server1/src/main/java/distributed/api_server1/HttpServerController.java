package distributed.api_server1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Scanner;

@Slf4j
@RestController
@RequestMapping("/api")
public class HttpServerController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String loadBalancerUrl = "http://localhost:8080/loadbalancer";

    @Value("${server.port}")
    private static int serverPort;

    public static void startConsole(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the port number for this server:");
        serverPort = Integer.parseInt(scanner.nextLine().trim());
    }

    // 서버 등록 및 해제 요청을 처리
    @GetMapping("/register")
    public String registerOrUnregister() {
        Scanner scanner = new Scanner(System.in);
        log.info("Enter 1 to register or 2 to unregister this server:");

        int userInput = scanner.nextInt();

        if (userInput == 1) {
            return registerServer();
        } else if (userInput == 2) {
            return unregisterServer();
        } else {
            return "Invalid input. Please enter 1 to register or 2 to unregister.";
        }
    }

    // 서버 등록
    private String registerServer() {
        String jsonRequest = String.format(
                "{\"cmd\":\"register\",\"protocol\":\"api\",\"port\":%d}", serverPort);
        log.info("Sending registration request: {}", jsonRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest, headers);

        try {
            String response = restTemplate.postForObject(
                    loadBalancerUrl + "/register", requestEntity, String.class);
            log.info("Response from LoadBalancer: {}", response);
            return "Registration successful: " + response;
        } catch (Exception e) {
            log.error("Failed to register with LoadBalancer", e);
            return "Registration failed: " + e.getMessage();
        }
    }

    // 서버 해제
    private String unregisterServer() {
        String jsonRequest = String.format(
                "{\"cmd\":\"unregister\",\"protocol\":\"api\",\"port\":%d}", serverPort);
        log.info("Sending unregister request: {}", jsonRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest, headers);

        try {
            String response = restTemplate.postForObject(
                    loadBalancerUrl + "/unregister", requestEntity, String.class);
            log.info("Response from LoadBalancer: {}", response);
            return "Unregistration successful: " + response;
        } catch (Exception e) {
            log.error("Failed to unregister from LoadBalancer", e);
            return "Unregistration failed: " + e.getMessage();
        }
    }

    // 헬스 체크 엔드포인트 (POST 요청 처리)
    @PostMapping("/health")
    public Map<String, String> healthCheck(@RequestBody Map<String, String> request) {
        log.info("Received health check request: {}", request);

        if ("hello".equals(request.get("cmd"))) {
            log.info("ACK sent.");
            return Map.of("ack", "hello");
        } else {
            log.warn("Invalid health check command received.");
            return Map.of("ack", "invalid");
        }
    }
}
