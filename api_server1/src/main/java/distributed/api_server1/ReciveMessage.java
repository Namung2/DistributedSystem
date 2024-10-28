package distributed.api_server1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class ReciveMessage {
    @PostMapping("/message")
    public String receiveMessage(@RequestParam String message){
        System.out.println(message);
        return message;
    }
}
