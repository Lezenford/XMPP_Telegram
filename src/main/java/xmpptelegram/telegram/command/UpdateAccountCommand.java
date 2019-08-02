package xmpptelegram.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.script.ScriptRegistry;
import xmpptelegram.telegram.script.factory.ScriptFactory;

@Component
public class UpdateAccountCommand extends BaseBotCommand {

    public UpdateAccountCommand(ScriptRegistry scriptRegistry,
                                ScriptFactory scriptFactory, TelegramUserRepository repository,
                                BotSentCallback botSentCallback) {
        super("update", "Update account setting", scriptRegistry, scriptFactory, repository, botSentCallback);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (checkUserRegister(absSender, user, chat) && checkPrivateChat(absSender, user, chat)) {
            scriptRegistry.register(absSender, user, chat, scriptFactory.getUpdateAccountScript());
        }
    }
}
