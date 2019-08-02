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
public class DeleteMeCommand extends BaseBotCommand {

    public DeleteMeCommand(ScriptRegistry scriptRegistry,
                           ScriptFactory scriptFactory, TelegramUserRepository repository,
                           BotSentCallback botSentCallback) {
        super("deleteme", "Remove user from xmpp", scriptRegistry, scriptFactory, repository, botSentCallback);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (checkUserRegister(absSender, user, chat) && checkPrivateChat(absSender, user, chat)) {
            SendMessage message = new SendMessage();
            TelegramUser telegramUser = repository.get(user.getId());
            if (telegramUser != null && repository.delete(telegramUser)) {
                message.setText("Пользователь удален");
            } else {
                message.setText("Ошибка удаления пользователя!");
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
