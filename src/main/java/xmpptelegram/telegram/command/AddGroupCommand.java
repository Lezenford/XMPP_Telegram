package xmpptelegram.telegram.command;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xmpptelegram.repository.jpa.ChatMapRepository;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.script.ScriptRegistry;
import xmpptelegram.telegram.script.factory.ScriptFactory;

@Log4j2
@Component
public class AddGroupCommand extends BaseBotCommand {

    private final ChatMapRepository chatMapRepository;

    public AddGroupCommand(ScriptRegistry scriptRegistry, ScriptFactory scriptFactory,
                           TelegramUserRepository repository, ChatMapRepository chatMapRepository,
                           BotSentCallback botSentCallback) {
        super("addgroup", "Register new group chat", scriptRegistry, scriptFactory, repository, botSentCallback);
        this.chatMapRepository = chatMapRepository;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (checkUserRegister(absSender, user, chat) && checkGroupChat(absSender, user, chat)) {
            if (chatMapRepository.get(chat.getId()) == null) {
                scriptRegistry.register(absSender, user, chat, scriptFactory.getAddGroupScript());
            } else {
                SendMessage message = new SendMessage(chat.getId(), "Данная группа уже используется.");
                try {
                    absSender.executeAsync(message, botSentCallback);
                } catch (TelegramApiException e) {
                    log.error(e);
                }
            }
        }
    }
}
