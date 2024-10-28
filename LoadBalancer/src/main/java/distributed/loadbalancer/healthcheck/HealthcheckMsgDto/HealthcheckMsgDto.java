package distributed.loadbalancer.healthcheck.HealthcheckMsgDto;

import lombok.Data;

@Data
public class HealthcheckMsgDto {
    public HealthcheckMsgDto(String cmd) {this.cmd = cmd;}
    private String cmd;
}
