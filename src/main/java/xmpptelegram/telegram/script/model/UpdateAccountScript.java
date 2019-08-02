package xmpptelegram.telegram.script.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
import xmpptelegram.telegram.script.querydata.InlineCommandType;
import xmpptelegram.telegram.script.querydata.QueryDataType;
import xmpptelegram.telegram.script.querydata.UpdateQueryData;
import xmpptelegram.xmpp.XMPPBot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateAccountScript extends BotScript {

    private final XMPPBot bot;

    private UpdateQueryData currentQuery; //last callback query for update

    private User user;

    public UpdateAccountScript(XMPPAccountRepository xmppAccountRepository,
                               TelegramUserRepository telegramUserRepository, XMPPBot bot,
                               BotSentCallback botSentCallback) {
        super(xmppAccountRepository, telegramUserRepository, botSentCallback);
        this.bot = bot;
    }

    @Override
    public void init(AbsSender absSender, User user, Chat chat) {
        chatId = chat.getId();
        this.user = user;
        try {
            SendMessage message;
            List<List<InlineKeyboardButton>> accountList = getAccountListButtons(user);
            if (accountList.size() > 0) {
                message = new SendMessage(chatId, "Выберите аккаунт для редактирования")
                        .setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(accountList));
            } else {
                message = new SendMessage(chatId, "Нет доступных аккаунтов для редактирования");
            }
            absSender.execute(message);
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
                UpdateQueryData data = mapper.readValue(update.getCallbackQuery().getData(), UpdateQueryData.class);
                if (data.getQueryDataType().equals(QueryDataType.UPDATE_ACCOUNT)) {
                    switch (data.getCommandType()) {
                        case SELECT_ACCOUNT: {
                            XMPPAccount xmppAccount = xmppAccountRepository.get(data.getAccountId());
                            message = new EditMessageText()
                                    .setChatId(update.getCallbackQuery().getMessage().getChatId())
                                    .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                                    .setText(String
                                            .format("Аккаунт: %s@%s\n\nСервер: %s\nЛогин: %s\nпорт: %s" +
                                                            "\nАктивировано: %s",
                                                    xmppAccount.getLogin(), xmppAccount.getServer(),
                                                    xmppAccount.getServer(), xmppAccount.getLogin(),
                                                    xmppAccount.getPort(), xmppAccount.isActive() ? "Да" : "Нет"))
                                    .setReplyMarkup(new InlineKeyboardMarkup()
                                            .setKeyboard(getAccountSettingButtons(data)));
                            break;
                        }
                        case EDIT_SERVER: {
                            message = new SendMessage(chatId, "Введите желаемый адрес сервера");
                            currentQuery = data;
                            break;
                        }
                        case EDIT_LOGIN: {
                            message = new SendMessage(chatId, "Введите желаемое имя пользователя");
                            currentQuery = data;
                            break;
                        }
                        case EDIT_PASSWORD: {
                            message = new SendMessage(chatId, "Введите требуемый пароль");
                            currentQuery = data;
                            break;
                        }
                        case EDIT_PORT: {
                            message = new SendMessage(chatId, "Введите желаемый порт сервера");
                            currentQuery = data;
                            break;
                        }
                        case DISABLE: {
                            XMPPAccount xmppAccount = xmppAccountRepository.get(data.getAccountId());
                            xmppAccount.setActive(!xmppAccount.isActive());
                            message = new EditMessageText()
                                    .setChatId(update.getCallbackQuery().getMessage().getChatId())
                                    .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                                    .setText(String
                                            .format("Аккаунт: %s@%s успешно %s",
                                                    xmppAccount.getLogin(), xmppAccount.getServer(),
                                                    xmppAccount.isActive() ? "Включен" : "Выключен"))
                                    .setReplyMarkup(new InlineKeyboardMarkup()
                                            .setKeyboard(getAccountSuccessUpdateButtons(currentQuery)));
                            currentQuery = data;
                            break;
                        }
                        case BACK_TO_ACCOUNT_LIST: {
                            List<List<InlineKeyboardButton>> accountList = getAccountListButtons(user);
                            if (accountList.size() > 0) {
                                message = new EditMessageText()
                                        .setChatId(update.getCallbackQuery().getMessage().getChatId())
                                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                                        .setText("Выберите аккаунт для редактирования")
                                        .setReplyMarkup(new InlineKeyboardMarkup().setKeyboard(accountList));
                            } else {
                                message = new SendMessage(chatId, "Нет доступных аккаунтов для редактирования");
                            }
                            break;
                        }
                    }
                }
            } else {
                if (currentQuery != null) {
                    XMPPAccount xmppAccount = xmppAccountRepository.get(currentQuery.getAccountId());
                    String text = update.getMessage().getText().strip();
                    switch (currentQuery.getCommandType()) {
                        case EDIT_SERVER: {
                            xmppAccount.setServer(text);
                            xmppAccountRepository.update(xmppAccount);
                            message = new SendMessage(chatId, "Адрес сервера успешно обновлен!")
                                    .setReplyMarkup(new InlineKeyboardMarkup()
                                            .setKeyboard(getAccountSuccessUpdateButtons(currentQuery)));
                            currentQuery = null;
                            break;
                        }
                        case EDIT_LOGIN: {
                            xmppAccount.setLogin(text);
                            xmppAccountRepository.update(xmppAccount);
                            message = new SendMessage(chatId, "Логин успешно обновлен!")
                                    .setReplyMarkup(new InlineKeyboardMarkup()
                                            .setKeyboard(getAccountSuccessUpdateButtons(currentQuery)));
                            currentQuery = null;
                            break;
                        }
                        case EDIT_PASSWORD: {
                            xmppAccount.setPassword(text);
                            xmppAccountRepository.update(xmppAccount);
                            message = new SendMessage(chatId, "Пароль успешно обновлен!")
                                    .setReplyMarkup(new InlineKeyboardMarkup()
                                            .setKeyboard(getAccountSuccessUpdateButtons(currentQuery)));
                            currentQuery = null;
                            break;
                        }
                        case EDIT_PORT: {
                            try {
                                int port = Integer.parseInt(text);
                                xmppAccount.setPort(port);
                                xmppAccountRepository.update(xmppAccount);
                                message = new SendMessage(chatId, "Порт сервера успешно обновлен!")
                                        .setReplyMarkup(new InlineKeyboardMarkup()
                                                .setKeyboard(getAccountSuccessUpdateButtons(currentQuery)));
                                currentQuery = null;
                            } catch (NumberFormatException e) {
                                log.error(String.format("Uncorrected port format! port: %s", text), e);
                                message = new SendMessage(chatId, "Невеный формат ввода! Допустимый диапазон значений" +
                                        " от 0 до 65535. Повторите ввод");
                            }
                            break;
                        }
                    }
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

    private List<List<InlineKeyboardButton>> getAccountListButtons(User user) throws JsonProcessingException {
        TelegramUser telegramUser = telegramUserRepository.get(user.getId());
        List<XMPPAccount> accounts = xmppAccountRepository.getAll(telegramUser);
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (XMPPAccount account : accounts) {
            buttons.add(Collections.singletonList(new InlineKeyboardButton(String
                    .format("%s@%s", account.getLogin(), account.getServer()))
                    .setCallbackData(mapper.writeValueAsString(
                            new UpdateQueryData(account.getId(), InlineCommandType.SELECT_ACCOUNT)))));
        }
        return buttons;
    }

    private List<List<InlineKeyboardButton>> getAccountSettingButtons(UpdateQueryData data) throws JsonProcessingException {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> line = new ArrayList<>();
        line.add(new InlineKeyboardButton("Изменить сервер")
                .setCallbackData(mapper.writeValueAsString(
                        new UpdateQueryData(data.getAccountId(), InlineCommandType.EDIT_SERVER))));
        line.add(new InlineKeyboardButton("Изменить логин")
                .setCallbackData(mapper.writeValueAsString(
                        new UpdateQueryData(data.getAccountId(), InlineCommandType.EDIT_LOGIN))));
        buttons.add(line);
        line = new ArrayList<>();
        line.add(new InlineKeyboardButton("Изменить пароль")
                .setCallbackData(mapper.writeValueAsString(
                        new UpdateQueryData(data.getAccountId(), InlineCommandType.EDIT_PASSWORD))));
        line.add(new InlineKeyboardButton("Изменить порт")
                .setCallbackData(mapper.writeValueAsString(
                        new UpdateQueryData(data.getAccountId(), InlineCommandType.EDIT_PORT))));
        buttons.add(line);
        XMPPAccount account = xmppAccountRepository.get(data.getAccountId());
        buttons.add(Collections.singletonList(new InlineKeyboardButton(account.isActive() ? "Отключить" : "Включить")
                .setCallbackData(mapper.writeValueAsString(
                        new UpdateQueryData(data.getAccountId(), InlineCommandType.DISABLE)))));
        buttons.add(Collections.singletonList(new InlineKeyboardButton("<< Назад")
                .setCallbackData(mapper.writeValueAsString(
                        new UpdateQueryData(data.getAccountId(), InlineCommandType.BACK_TO_ACCOUNT_LIST)))));
        return buttons;
    }

    private List<List<InlineKeyboardButton>> getAccountSuccessUpdateButtons(UpdateQueryData data) throws JsonProcessingException {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> line = new ArrayList<>();
        line.add(new InlineKeyboardButton("<< Вернуться к аккаунту")
                .setCallbackData(mapper.writeValueAsString(
                        new UpdateQueryData(data.getAccountId(), InlineCommandType.SELECT_ACCOUNT))));
        line.add(new InlineKeyboardButton("<< Вернуться к списку аккаунтов")
                .setCallbackData(mapper.writeValueAsString(
                        new UpdateQueryData(data.getAccountId(), InlineCommandType.BACK_TO_ACCOUNT_LIST))));
        buttons.add(line);
        XMPPAccount xmppAccount = xmppAccountRepository.get(data.getAccountId());
        bot.connectAccount(xmppAccount);
        return buttons;
    }
}
