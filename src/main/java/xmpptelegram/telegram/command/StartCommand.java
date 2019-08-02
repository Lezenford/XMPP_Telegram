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
public class StartCommand extends BaseBotCommand {

    public StartCommand(ScriptRegistry scriptRegistry,
                        ScriptFactory scriptFactory, TelegramUserRepository repository,
                        BotSentCallback botSentCallback) {
        super("start", "Register new user", scriptRegistry, scriptFactory, repository, botSentCallback);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (checkPrivateChat(absSender, user, chat)) {
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId());
            TelegramUser telegramUser = repository.get(user.getId());
            if (telegramUser == null) {
                telegramUser = new TelegramUser(user.getId(), user.getUserName());
                if (repository.create(telegramUser)) {
                    message.setText("Пользователь успешно зарегистрирован");
                } else
                    message.setText("Ошибка создания нового пользователя!");
            } else
                message.setText("Пользователь уже существует");
            try {
                absSender.executeAsync(message, botSentCallback);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
}
