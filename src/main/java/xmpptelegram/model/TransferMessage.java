package xmpptelegram.model;

import lombok.Data;

import java.util.Date;

@Data
public class TransferMessage {

    private String text;

    private ChatMap chatMap;

    private boolean fromXMPP;

    private Date date;

    public TransferMessage() {
        date = new Date();
    }

    public void setText(String text) {
        if (text.length() > 4096) { //передача через телеграм сообщений длиннее невозможна
            this.text = text.substring(0, 4095);
        } else {
            this.text = text;
        }
    }

    public TransferMessage(ChatMap map, String text, boolean fromXMPP) {
        this.chatMap = map;
        if (text.length() > 4096) {
            text = text.substring(0, 4095);
        }
        this.text = text;
        this.fromXMPP = fromXMPP;
        date = new Date();
    }
}
