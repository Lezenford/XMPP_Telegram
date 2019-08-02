package xmpptelegram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.UnsentMessage;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.repository.jpa.ChatMapRepository;
import xmpptelegram.repository.jpa.MessageRepository;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.TelegramBot;
import xmpptelegram.xmpp.XMPPBot;

import java.util.Date;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final ChatMapRepository chatMapRepository;

    private final TelegramBot telegramBot;

    private final XMPPBot xmppBot;

    private final UnmappedChatQueries unmappedChatQueries;

    private final BotSentCallback botSentCallback;

    @Scheduled(fixedDelay = 60000L, initialDelay = 10000L)
    private void checkUnsentMessages() {
        log.debug("checkUnsentMessages");
        List<UnsentMessage> list = messageRepository.getAll();
        for (UnsentMessage message : list) {
            if (message.isFromXMPP()) {
                messageRepository.delete(message);
                send(message.getXmppAccount(), message.getXmppContact(), message.getText());
            }
        }
    }

    public void send(Update update) {
        ChatMap chatMap = chatMapRepository.get(update.getMessage().getChatId());
        if (chatMap != null) {
            if (!xmppBot.sendMessage(chatMap, update.getMessage().getText())) {
                SendMessage sendMessage = new SendMessage(update.getMessage()
                                                                .getChatId(), "Сообщение не было доставлено " +
                        "пользователю, попробуйте повторить позднее!");
                sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
                try {
                    telegramBot.executeAsync(sendMessage, botSentCallback);
                } catch (TelegramApiException e) {
                    log.error(e);
                }
            }
        }
    }

    public void send(XMPPAccount account, String contact, String text) {
        ChatMap map = chatMapRepository.get(account, contact);
        SendMessage message;
        if (map == null) {
            if (contact != null) { //null - для служебных сообщений
                unmappedChatQueries.addAccountChat(account, contact);
                text = String.format("Сообщение для аккаунта: %s@%s от контакта: %s \n%s",
                        account.getLogin(), account.getServer(), contact, text);
            }
            message = new SendMessage(account.getTelegramUser().getDefaultChat(), text);
        } else {
            message = new SendMessage(map.getChatId(), text);
        }
        try {
            telegramBot.executeAsync(message, botSentCallback);
        } catch (TelegramApiException e) {
            UnsentMessage unsentMessage = new UnsentMessage();
            unsentMessage.setDate(new Date());
            unsentMessage.setText(text);
            unsentMessage.setFromXMPP(true);
            unsentMessage.setXmppAccount(account);
            unsentMessage.setXmppContact(contact);
            messageRepository.create(unsentMessage);
            log.error("Can't send message to Telegram!", e);
        }
    }
}
