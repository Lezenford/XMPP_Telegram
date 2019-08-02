package xmpptelegram.telegram.command;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xmpptelegram.model.TelegramUser;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.repository.jpa.XMPPAccountRepository;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.script.ScriptRegistry;
import xmpptelegram.telegram.script.factory.ScriptFactory;
import xmpptelegram.xmpp.XMPPBot;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Log4j2
@Component
public class ReconnectCommand extends BaseBotCommand {
    private final XMPPAccountRepository xmppAccountRepository;
    private final XMPPBot xmppBot;
    private final ExecutorService threadPool;

    public ReconnectCommand(ScriptRegistry scriptRegistry,
                            ScriptFactory scriptFactory, TelegramUserRepository repository,
                            XMPPAccountRepository xmppAccountRepository, XMPPBot xmppBot, ExecutorService threadPool,
                            BotSentCallback botSentCallback) {
        super("reconnect", "Reconnect all user's XMPP-accounts", scriptRegistry, scriptFactory, repository,
                botSentCallback);
        this.xmppAccountRepository = xmppAccountRepository;
        this.xmppBot = xmppBot;
        this.threadPool = threadPool;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (checkUserRegister(absSender, user, chat) && checkPrivateChat(absSender, user, chat)) {
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId());
            TelegramUser telegramUser = repository.get(user.getId());
            List<XMPPAccount> accounts = xmppAccountRepository.getAll(telegramUser);
            if (accounts == null) {
                message.setText("Нет доступных XMPP-аккаунтов");
            } else {
                threadPool.execute(() -> {
                    for (XMPPAccount account : accounts) {
                        xmppBot.disconnectAccount(account);
                        xmppBot.connectAccount(account);
                    }
                });
                message.setText("Команда выполнена успешно");
            }
            try {
                absSender.executeAsync(message, botSentCallback);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
}
