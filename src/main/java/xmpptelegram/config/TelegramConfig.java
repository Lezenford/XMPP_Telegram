package xmpptelegram.config;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import xmpptelegram.telegram.TelegramBot;
import xmpptelegram.telegram.script.ScriptRegistry;

import java.util.Collection;

@Log4j2
@Data
@Configuration
public class TelegramConfig {

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.username}")
    private String username;

    @Value("${telegram.path}")
    private String path;

    @Value("${telegram.cert}")
    private String cert;

    @Bean
    public TelegramBot telegramBot(Collection<BotCommand> botCommands, ScriptRegistry scriptRegistry) {
        TelegramBot bot = new TelegramBot(this, scriptRegistry);
        botCommands.forEach(bot::register);
        return bot;
    }
}
