package xmpptelegram.telegram.command;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xmpptelegram.model.TelegramUser;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.script.ScriptRegistry;
import xmpptelegram.telegram.script.factory.ScriptFactory;

@Log4j2
@Component
public class DefaultCommand extends BaseBotCommand {

    public DefaultCommand(ScriptRegistry scriptRegistry,
                          ScriptFactory scriptFactory, TelegramUserRepository repository,
                          BotSentCallback botSentCallback) {
        super("default", "Set default user chat", scriptRegistry, scriptFactory, repository, botSentCallback);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (checkUserRegister(absSender, user, chat)) {
            TelegramUser telegramUser = repository.get(user.getId());
            SendMessage message = new SendMessage();
            if (telegramUser.getDefaultChat() == chat.getId()) {
                message.setText("Этот чат уже является чатом по-умолчанию");
            } else {
                telegramUser.setDefaultChat(chat.getId());
                if (repository.update(telegramUser) != null) {
                    message.setText("Чат по-умолчанию переопределен");
                } else
                    message.setText("Ошибка переопределения чата по-умолчанию!");
            }
            try {
                message.setChatId(chat.getId());
                absSender.executeAsync(message, botSentCallback);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
}
