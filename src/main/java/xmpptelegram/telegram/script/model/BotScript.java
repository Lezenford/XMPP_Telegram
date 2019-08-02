package xmpptelegram.telegram.script.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.repository.jpa.XMPPAccountRepository;
import xmpptelegram.telegram.BotSentCallback;

@RequiredArgsConstructor
public abstract class BotScript {
    final XMPPAccountRepository xmppAccountRepository;
    final TelegramUserRepository telegramUserRepository;
    final BotSentCallback botSentCallback;
    final ObjectMapper mapper = new ObjectMapper();

    @Getter
    boolean finished = false;

    @Getter
    Long chatId;

    public abstract void init(AbsSender absSender, User user, Chat chat);

    public abstract void execute(AbsSender absSender, Update update);

}