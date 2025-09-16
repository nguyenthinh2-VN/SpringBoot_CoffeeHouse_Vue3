package SpringBoot.demo.Controller;

import SpringBoot.demo.Model.MessageModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {
    @GetMapping("/message")
    public MessageModel getMessage() {
        return new MessageModel("Xin chao", "Thinh");
    }
}
