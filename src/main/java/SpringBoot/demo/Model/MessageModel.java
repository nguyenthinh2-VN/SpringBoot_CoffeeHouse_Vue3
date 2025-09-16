package SpringBoot.demo.Model;

import lombok.Getter;

@Getter
public class MessageModel {
    public String text;
    public String author;

    public MessageModel(String text, String author) {
        this.text = text;
        this.author = author;
    }
}
