package xmpptelegram.telegram;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

@Log4j2
@Component
public class BotSentCallback implements SentCallback<Message> {

    @Override
    public void onError(BotApiMethod method, TelegramApiRequestException apiException) {
        log.error(apiException);
    }

    @Override
    public void onException(BotApiMethod method, Exception exception) {
        log.error(exception);
    }

    @Override
    public void onResult(BotApiMethod<Message> method, Message response) {

    }
}