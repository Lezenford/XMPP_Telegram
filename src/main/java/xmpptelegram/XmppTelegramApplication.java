package xmpptelegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import xmpptelegram.bot.TelegramBot;
import xmpptelegram.bot.XMPPBot;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class XmppTelegramApplication implements CommandLineRunner {

    @Autowired
    private XMPPBot xmppBot;

    @Autowired
    private TelegramBot telegramBot;

    public static void main(String[] args) {
        SpringApplication.run(XmppTelegramApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        telegramBot.init();
        xmppBot.start();
    }
}
