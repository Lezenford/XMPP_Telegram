package xmpptelegram.telegram.script.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.TelegramUser;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.repository.jpa.ChatMapRepository;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.repository.jpa.XMPPAccountRepository;
import xmpptelegram.service.UnmappedChatQueries;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.script.querydata.AddGroupQueryData;
import xmpptelegram.telegram.script.querydata.InlineCommandType;
import xmpptelegram.telegram.script.querydata.QueryDataType;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AddGroupScript extends BotScript {

    private final UnmappedChatQueries unmappedChatQueries;
    private final ChatMapRepository chatMapRepository;


    private XMPPAccount selectedAccount = null;

    public AddGroupScript(XMPPAccountRepository xmppAccountRepository, TelegramUserRepository telegramUserRepository,
                          UnmappedChatQueries unmappedChatQueries, ChatMapRepository chatMapRepository,
                          BotSentCallback botSentCallback) {
        super(xmppAccountRepository, telegramUserRepository, botSentCallback);
        this.unmappedChatQueries = unmappedChatQueries;
        this.chatMapRepository = chatMapRepository;
    }

    @Override
    public void init(AbsSender absSender, User user, Chat chat) {
        chatId = chat.getId();
        try {
            SendMessage message;
            List<List<InlineKeyboardButton>> accountList = getAccountListButtons(user);
            if (accountList.size() > 0) {
                message = new SendMessage(chatId, "Выберите аккаунт для связывания его контакта с данной группой")
                        .setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(accountList));
            } else {
                message = new SendMessage(chatId, "Нет доступных аккаунтов");
            }
            absSender.executeAsync(message, botSentCallback);
        } catch (IOException e) {
            log.error("JSON-mapper exception", e);
        } catch (TelegramApiException e) {
            log.error("Send telegram message error", e);
        }
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        try {
            BotApiMethod<?> message = null;
            if (update.hasCallbackQuery()) {
                AddGroupQueryData queryData = mapper
                        .readValue(update.getCallbackQuery().getData(), AddGroupQueryData.class);
                if (queryData.getQueryDataType().equals(QueryDataType.ADD_GROUP)) {
                    switch (queryData.getCommandType()) {
                        case SELECT_ACCOUNT: {
                            XMPPAccount account = xmppAccountRepository.get(Integer.parseInt(queryData.getData()));
                            Set<String> contacts = unmappedChatQueries.getAccountChats(account);
                            message = new EditMessageText()
                                    .setChatId(update.getCallbackQuery().getMessage().getChatId())
                                    .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                                    .setText("Введите имя контакта или выберите его из списка ниже");

                            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                            for (String contact : contacts) {
                                String json = mapper
                                        .writeValueAsString(new AddGroupQueryData(InlineCommandType.SELECT_CONTACT,
                                                contact));
                                if (json.getBytes().length <= 64) {
                                    buttons.add(Collections.singletonList(new InlineKeyboardButton(contact)
                                            .setCallbackData(json)));
                                }
                            }
                            buttons.add(Collections
                                    .singletonList(new InlineKeyboardButton("<< Вернуться к списку аккаунтов")
                                            .setCallbackData(mapper.writeValueAsString(
                                                    new AddGroupQueryData(InlineCommandType.BACK_TO_ACCOUNT_LIST,
                                                            null)))));
                            ((EditMessageText) message)
                                    .setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(buttons));

                            selectedAccount = account;
                            break;
                        }
                        case BACK_TO_ACCOUNT_LIST: {
                            List<List<InlineKeyboardButton>> accounts =
                                    getAccountListButtons(update.getCallbackQuery().getMessage().getFrom());
                            if (accounts.size() > 0) {
                                message = new EditMessageText()
                                        .setText("Выберите аккаунт для связывания его контакта с данной группой")
                                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                                        .setChatId(update.getCallbackQuery().getMessage().getChatId())
                                        .setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(accounts));
                            } else {
                                message = new EditMessageText()
                                        .setChatId(update.getCallbackQuery().getMessage().getChatId())
                                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                                        .setReplyMarkup(null)
                                        .setText("Нет доступных аккаунтов");
                            }
                            selectedAccount = null;
                            break;

                        }
                        case SELECT_CONTACT: {
                            message = createChatMap(update.getCallbackQuery().getMessage(), queryData
                                    .getData(), absSender);
                            selectedAccount = null;
                            break;
                        }
                    }
                }
            } else {
                if (selectedAccount != null) {
                    message = createChatMap(update.getMessage(), update.getMessage().getText(), absSender);
                    selectedAccount = null;
                }
            }
            if (message != null) {
                absSender.execute(message);
            }
        } catch (IOException e) {
            log.error("JSON-mapper exception", e);
        } catch (TelegramApiException e) {
            log.error("Send telegram message error", e);
        }
    }

    private SendMessage createChatMap(Message message, String contact, AbsSender absSender) throws TelegramApiException {
        ChatMap chatMap = new ChatMap();
        chatMap.setXmppAccount(selectedAccount);
        chatMap.setXmppContact(contact);
        chatMap.setChatId(message.getChatId());
        EditMessageReplyMarkup closeKeyboard = new EditMessageReplyMarkup().setChatId(message.getChatId())
                                                                           .setMessageId(message.getMessageId());
        SendMessage sendMessage = new SendMessage().setChatId(message.getChatId());
        if (chatMapRepository.create(chatMap)) {
            sendMessage.setText("Группа успешно создана");
            unmappedChatQueries.removeAccountChat(selectedAccount, contact);
        } else {
            sendMessage.setText("Ошибка создания группы!");
        }
        selectedAccount = null;
        absSender.executeAsync(closeKeyboard, new SentCallback<>() {
            @Override
            public void onResult(BotApiMethod<Serializable> method, Serializable response) {

            }

            @Override
            public void onError(BotApiMethod<Serializable> method, TelegramApiRequestException apiException) {
                log.error(apiException);
            }

            @Override
            public void onException(BotApiMethod<Serializable> method, Exception exception) {
                log.error(exception);
            }
        });
        finished = true;
        return sendMessage;
    }

    private List<List<InlineKeyboardButton>> getAccountListButtons(User user) throws JsonProcessingException {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        TelegramUser telegramUser = telegramUserRepository.get(user.getId());
        if (telegramUser != null) {
            List<XMPPAccount> accounts = xmppAccountRepository.getAll(telegramUser);
            for (XMPPAccount account : accounts) {
                buttons.add(Collections.singletonList(new InlineKeyboardButton(String
                        .format("%s@%s", account.getLogin(), account.getServer()))
                        .setCallbackData(mapper.writeValueAsString(
                                new AddGroupQueryData(InlineCommandType.SELECT_ACCOUNT, String
                                        .valueOf(account.getId()))))));
            }
        } else {
            log.error(String.format("Can't find telegram user! User id: %s", user.getId()));
        }
        return buttons;
    }
}
