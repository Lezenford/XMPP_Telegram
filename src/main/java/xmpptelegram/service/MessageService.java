package xmpptelegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updateshandlers.SentCallback;
import xmpptelegram.bot.TelegramBot;
import xmpptelegram.bot.XMPPBot;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.TransferMessage;
import xmpptelegram.model.UnsentMessage;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.repository.jpa.MessageRepository;

import java.util.List;

@Slf4j
@Service
public class MessageService {

    @Autowired
    private MessageRepository repository;

    @Autowired
    private ChatMapService chatMapService;

    @Autowired
    private XMPPAccountService xmppAccountService;

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private XMPPBot xmppBot;

    @Autowired
    private TelegramCommandService telegramCommandService;

    @Scheduled(fixedDelay = 60000L, initialDelay = 10000L)
    private void checkUnsentMessages() {
        log.debug("checkUnsentMessages");
        List<UnsentMessage> list = getAll();
        for (UnsentMessage message : list) {
            ChatMap map = chatMapService.getByAccountAndContact(message.getXmppAccount(), message.getXmppContact());
            if (map != null) {
                TransferMessage transferMessage = new TransferMessage();
                transferMessage.setFromXMPP(message.isFromXMPP());
                transferMessage.setText(message.getText());
                transferMessage.setDate(message.getDate());
                transferMessage.setChatMap(map);
                repository.delete(message);
                send(transferMessage, false);
            } else if (message.isFromXMPP()) {
                repository.delete(message);
                send(message.getXmppAccount().getServer(), message.getXmppAccount().getLogin(), message.getXmppContact(),
                        message.getText());
            }
        }
    }

    public List<UnsentMessage> getAll() {
        return repository.getAll();
    }

    public void send(TransferMessage transferMessage, boolean notification) {
        if (notification) {
            transferMessage.setFromXMPP(!transferMessage.isFromXMPP());
        }
        if (transferMessage.isFromXMPP()) {
            SendMessage message = new SendMessage();
            message.setChatId(transferMessage.getChatMap().getChatId());
            message.setText(transferMessage.getText());
            try {
                telegramBot.executeAsync(message, new SentCallback<Message>() {
                    @Override
                    public void onResult(BotApiMethod<Message> method, Message response) {

                    }

                    @Override
                    public void onError(BotApiMethod<Message> method, TelegramApiRequestException apiException) {
                        log.error(String.format("Error sending message to Telegram! Message: %s", transferMessage.toString()),
                                apiException);
                        repository.create(new UnsentMessage(transferMessage));
                    }

                    @Override
                    public void onException(BotApiMethod<Message> method, Exception exception) {
                        log.error(String.format("TelegramAPI exception! Message: %s", transferMessage.toString()), exception);
                        repository.create(new UnsentMessage(transferMessage));
                    }
                });
            } catch (TelegramApiException e) {
                log.error("TelegramAPI error! Messages can't be sent!", e);
                repository.create(new UnsentMessage(transferMessage));
            }
        } else {
            XMPPBot.threadPool.execute(() -> {
                if (!xmppBot.sendXMPPMessage(transferMessage)) {
                    transferMessage.setText("Сообщение не доставлено получателю! Вероятно не удалось подключиться к XMPP-серверу! " +
                            "Проверьте статус подключения и попробуйте еще раз!");
                    send(transferMessage, true);
                }
            });
        }

    }

    public void send(Update update) {
        TransferMessage message = new TransferMessage();
        message.setChatMap(chatMapService.getByChatId(update.getMessage().getChatId()));
        message.setFromXMPP(false);
        if (update.getMessage().getText().matches("^[/].+")) {
            if (message.getChatMap() == null) {
                ChatMap map = new ChatMap();
                map.setChatId(update.getMessage().getChatId());
                message.setChatMap(map);
            }
            message.setText(telegramCommandService.useCommand(update));
            send(message, true);
        } else {
            if (update.getMessage().getChatId().equals((long) update.getMessage().getFrom().getId()) || message.getChatMap() == null) {
                message.setText("Неверный чат! Сообщение не будет доставлено - нет получателя!");
                ChatMap map = new ChatMap();
                map.setChatId(update.getMessage().getChatId());
                message.setChatMap(map);
                send(message, true);
                log.warn("Incorrect chat. ", update.toString());
                return;
            }
            message.setText(update.getMessage().getText());
            send(message, false);
        }
    }

    public void send(String server, String login, String contact, String text) {
        XMPPAccount account = xmppAccountService.get(server, login);
        if (account == null) {
            log.error(String.format("Can't find account info! Message from XMPP didn't send! Server: %s, login: %s", server, login));
            return;
        }
        ChatMap map = chatMapService.getByAccountAndContact(account, contact);
        TransferMessage message = new TransferMessage();
        message.setFromXMPP(true);
        if (map == null) {
            map = new ChatMap();
            map.setXmppAccount(account);
            map.setXmppContact(contact);
            map.setChatId(account.getTelegramUser().getDefaultChat());
            if (contact != null) { //null - когда отправляем просто сообщение о подключении\отключении
                text = String.format("Сообщение для аккаунта: %s от контакта: %s \n%s", account.getLogin() + "@" + account.getServer(),
                        contact, text);
            }
        }
        message.setChatMap(map);
        message.setText(text);
        send(message, false);
    }
}
