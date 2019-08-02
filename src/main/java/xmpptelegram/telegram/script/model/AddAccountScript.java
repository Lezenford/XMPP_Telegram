package xmpptelegram.telegram.script.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xmpptelegram.model.TelegramUser;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.repository.jpa.XMPPAccountRepository;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.script.querydata.AddAccountQueryData;
import xmpptelegram.telegram.script.querydata.QueryDataType;
import xmpptelegram.xmpp.XMPPBot;

import java.io.IOException;
import java.util.Collections;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AddAccountScript extends BotScript {
    private static final int USER_NAME_REQUEST = 1;
    private static final int USER_PASSWORD_REQUEST = 2;
    private static final int SERVER_ADDRESS_REQUEST = 3;
    private static final int SERVER_PORT_REQUEST = 4;

    private final XMPPBot xmppBot;

    private ObjectMapper mapper = new ObjectMapper();

    private int step = 0;

    private XMPPAccount account = new XMPPAccount();

    public AddAccountScript(XMPPAccountRepository xmppAccountRepository,
                            TelegramUserRepository telegramUserRepository, XMPPBot xmppBot,
                            BotSentCallback botSentCallback) {
        super(xmppAccountRepository, telegramUserRepository, botSentCallback);
        this.xmppBot = xmppBot;
    }

    @Override
    public void init(AbsSender absSender, User user, Chat chat) {
        chatId = chat.getId();
        try {
            absSender.execute(initLoginRequest());
        } catch (TelegramApiException e) {
            log.error("Send telegram message error", e);
        }
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        String text = null;
        int userId;
        SendMessage message;
        try {
            if (update.hasCallbackQuery()) {
                userId = update.getCallbackQuery().getFrom().getId();
                try {
                    AddAccountQueryData data = mapper
                            .readValue(update.getCallbackQuery().getData(), AddAccountQueryData.class);
                    if (data.getQueryDataType().equals(QueryDataType.ADD_ACCOUNT)) {
                        text = data.getData();
                    }
                } catch (IOException e) {
                    log.error("JSON-mapper exception", e);
                }
            } else {
                text = update.getMessage().getText();
                userId = update.getMessage().getFrom().getId();
            }
            if (text != null) {
                switch (step) {
                    case USER_NAME_REQUEST: {
                        text = text.strip();
                        if (!text.isBlank() && !text.contains(" ")) {
                            String[] args = text.split("@");
                            if (args.length == 1) {
                                account.setLogin(args[0]);
                            }
                            if (args.length == 2) {
                                account.setLogin(args[0]);
                                account.setServer(args[1]);
                            }
                        }
                        if (account.getLogin() == null) {
                            message = new SendMessage(chatId, "Некверный формат имени пользователя! Повторите ввод");
                        } else {
                            message = initPasswordRequest();
                        }
                        break;
                    }
                    case USER_PASSWORD_REQUEST: {
                        text = text.strip();
                        if (text.isBlank() || text.contains(" ")) {
                            message = new SendMessage(chatId, "Неверный формат пароля! Повторите попытку ввода");
                        } else {
                            account.setPassword(text);
                            if (account.getServer() == null) {
                                message = initServerAddressRequest();
                            } else {
                                message = initServerPortRequest();
                            }
                        }
                        break;
                    }
                    case SERVER_ADDRESS_REQUEST: {
                        text = text.strip();
                        if (text == null || text.isBlank() || text.contains(" ")) {
                            message = new SendMessage(chatId, "Некверный формат адреса сервера! Повторите ввод");
                        } else {
                            account.setServer(text);
                            message = initServerPortRequest();
                        }
                        break;
                    }
                    case SERVER_PORT_REQUEST: {
                        text = text.strip();
                        int port;
                        try {
                            port = Integer.parseInt(text);
                        } catch (NumberFormatException e) {
                            port = 0;
                        }
                        if (text == null || text.isBlank() || text.contains(" ") || port == 0) {
                            message = new SendMessage(chatId, "Некорректный формат порта! Повторите ввод");
                        } else {
                            account.setPort(port);
                            TelegramUser telegramUser = telegramUserRepository.get(userId);
                            account.setTelegramUser(telegramUser);
                            if (xmppAccountRepository.create(account)) {
                                message = new SendMessage(chatId, "Аккаунт успешно добавилен");
                                finished = true;
                                xmppBot.connectAccount(account);
                            } else {
                                message = new SendMessage(chatId, "Ошибка добавления нового пользователя. Сценарий " +
                                        "прерван, попробуйте еще раз.");
                            }
                        }
                        break;
                    }
                    default: {
                        message = new SendMessage(chatId, "Ошибка добавления нового пользователя. Сценарий прерван, " +
                                "попробуйте еще раз.");
                    }
                }
            } else {
                message = new SendMessage(chatId, "Ошибка сценария!");
            }
            absSender.execute(message);
        } catch (IOException e) {
            log.error("JSON-mapper exception", e);
        } catch (TelegramApiException e) {
            log.error("Send telegram message error", e);
        }
    }

    private SendMessage initLoginRequest() {
        SendMessage result = new SendMessage(chatId, "Введите имя пользователя");
        step = USER_NAME_REQUEST;
        return result;
    }

    private SendMessage initPasswordRequest() {
        SendMessage result = new SendMessage(chatId, "Введите пароль пользователя");
        step = USER_PASSWORD_REQUEST;
        return result;
    }

    private SendMessage initServerAddressRequest() {
        SendMessage result = new SendMessage(chatId, "Введите адрес сервера");
        step = SERVER_ADDRESS_REQUEST;
        return result;
    }

    private SendMessage initServerPortRequest() throws JsonProcessingException {
        SendMessage message = new SendMessage(chatId, "Введите порт сервера, либо используйте стандартный")
                .setReplyMarkup(new InlineKeyboardMarkup()
                        .setKeyboard(Collections.singletonList(Collections.singletonList(
                                new InlineKeyboardButton("Использовать стандартный")
                                        .setCallbackData(mapper.writeValueAsString(
                                                new AddAccountQueryData("5222")))))));
        step = SERVER_PORT_REQUEST;
        return message;
    }
}
