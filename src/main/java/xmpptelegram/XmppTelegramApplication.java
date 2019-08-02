package xmpptelegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import xmpptelegram.config.TelegramConfig;
import xmpptelegram.telegram.TelegramBot;
import xmpptelegram.xmpp.XMPPBot;

@Log4j2
@PropertySource(value = "file:external.properties")
@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class XmppTelegramApplication implements CommandLineRunner {

    private final TelegramConfig telegramConfig;
    private final XMPPBot xmppBot;
    private final TelegramBot telegramBot;

    public static void main(String[] args) {
        SpringApplication.run(XmppTelegramApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        telegramBot.setWebhook(telegramConfig.getPath() + telegramConfig.getToken(), telegramConfig.getCert());
        xmppBot.start();
    }
}
