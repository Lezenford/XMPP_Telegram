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

@Log4j2
@Component
public class StatusCommand extends BaseBotCommand {

    private final XMPPAccountRepository xmppAccountRepository;
    private final XMPPBot xmppBot;

    public StatusCommand(ScriptRegistry scriptRegistry,
                         ScriptFactory scriptFactory, TelegramUserRepository repository,
                         XMPPAccountRepository xmppAccountRepository,
                         XMPPBot xmppBot, BotSentCallback botSentCallback) {
        super("status", "Current account status", scriptRegistry, scriptFactory, repository, botSentCallback);
        this.xmppAccountRepository = xmppAccountRepository;
        this.xmppBot = xmppBot;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (checkUserRegister(absSender, user, chat) && checkPrivateChat(absSender, user, chat)) {
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId());
            TelegramUser telegramUser = repository.get(user.getId());
            List<XMPPAccount> xmppAccounts = xmppAccountRepository.getAll(telegramUser);
            if (xmppAccounts.size() == 0) {
                message.setText("Нет активных XMPP-аккаунтов");
            } else {
                StringBuilder result = new StringBuilder();
                for (XMPPAccount account : xmppAccounts) {
                    if (account.isActive()) {
                        result.append(String.format("Аккаунт %s@%s: %s\n", account.getLogin(), account.getServer(),
                                xmppBot.checkConnection(account) ? "В сети" : "Не в сети"));
                    } else {
                        result.append(String
                                .format("Аккаунт %s@%s: %s\n", account.getLogin(), account.getServer(), "Отключен"));
                    }
                }
                message.setText(result.toString());
            }
            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
}
