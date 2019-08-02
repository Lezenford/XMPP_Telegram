package xmpptelegram.telegram.command;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.script.ScriptRegistry;
import xmpptelegram.telegram.script.factory.ScriptFactory;

@Log4j2
@Component
public class HelpCommand extends BaseBotCommand {

    public HelpCommand(ScriptRegistry scriptRegistry,
                       ScriptFactory scriptFactory, TelegramUserRepository repository,
                       BotSentCallback botSentCallback) {
        super("help", "Help command", scriptRegistry, scriptFactory, repository, botSentCallback);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        String text = "/start - регистрация нового пользователя\n" +
                "/status - статус подключения XMPP-аккаунтов\n" +
                "/reconnect - переподключить XMPP-аккаунты\n" +
                "/default - выбор чата по-умолчанию для всех сообщений\n" +
                "/addaccount - заведение нового XMPP-аккаунта\n" +
                "/update - обновление данных XMPP-аккаунта\n" +
                "/addgroup - переадресация в текущую группу сообщений данного XMPP-contact (работает только в " +
                "групповых чатах)\n" +
                "/deletegroup - отключение переадресации в текущую группу\n" +
                "/deleteme - отключение от бота\n";
        SendMessage message = new SendMessage(chat.getId(), text);
        try {
            absSender.executeAsync(message, botSentCallback);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }
}
