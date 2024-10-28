package distributed.loadbalancer.healthcheck.controller;

import distributed.loadbalancer.healthcheck.HealthcheckMsgDto.HealthcheckMsgDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @PostMapping
    public Map<String, String> healthCheck(@RequestBody HealthcheckMsgDto healthcheckMsgDto) {
        if ("hello".equals(healthcheckMsgDto.getCmd())) {
            // "cmd"가 "hello"이면 "ack":"hello" 응답 반환
            return Map.of("ack", "hello");
        }
        // 예상치 못한 메시지의 경우 빈 응답 반환
        return Map.of();
    }
}
