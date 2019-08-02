package xmpptelegram.telegram.command;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
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
public abstract class BaseBotCommand extends BotCommand {

    final ScriptRegistry scriptRegistry;
    final ScriptFactory scriptFactory;
    final TelegramUserRepository repository;
    final BotSentCallback botSentCallback;

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param scriptRegistry    spring bean
     * @param scriptFactory     spring bean
     * @param repository        spring bean
     * @param botSentCallback   spring bean
     */
    public BaseBotCommand(String commandIdentifier, String description, ScriptRegistry scriptRegistry,
                          ScriptFactory scriptFactory, TelegramUserRepository repository,
                          BotSentCallback botSentCallback) {
        super(commandIdentifier, description);
        this.scriptRegistry = scriptRegistry;
        this.scriptFactory = scriptFactory;
        this.repository = repository;
        this.botSentCallback = botSentCallback;
    }

    boolean checkUserRegister(AbsSender absSender, User user, Chat chat) {
        TelegramUser telegramUser = repository.get(user.getId());
        if (telegramUser == null) {
            SendMessage message = new SendMessage(chat
                    .getId(), "Telegram-пользователь не зарегистрирован, воспользуйтесь командой /start");
            try {
                absSender.executeAsync(message, botSentCallback);
            } catch (TelegramApiException e) {
                log.error(e);
            }
            return false;
        } else {
            return true;
        }
    }

    boolean checkGroupChat(AbsSender absSender, User user, Chat chat) {
        boolean privateChat = ((long) user.getId()) == chat.getId();
        if (privateChat) {
            SendMessage message = new SendMessage(chat.getId(),
                    "Эту команду можно использовать только в групповом чате!");
            try {
                absSender.executeAsync(message, botSentCallback);
            } catch (TelegramApiException e) {
                log.error(e);
            }
            return false;
        }
        return true;
    }

    boolean checkPrivateChat(AbsSender absSender, User user, Chat chat) {
        boolean privateChat = ((long) user.getId()) == chat.getId();
        if (!privateChat) {
            SendMessage message = new SendMessage(chat.getId(),
                    "Эту команду нельзя использовать в групповом чате!");
            try {
                absSender.executeAsync(message, botSentCallback);
            } catch (TelegramApiException e) {
                log.error(e);
            }
            return false;
        }
        return true;
    }
}
