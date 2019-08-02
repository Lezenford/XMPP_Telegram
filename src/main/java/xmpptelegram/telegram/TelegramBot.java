package xmpptelegram.telegram;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.CommandRegistry;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import xmpptelegram.config.TelegramConfig;
import xmpptelegram.service.MessageService;
import xmpptelegram.telegram.script.ScriptRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;


@Log4j2
public class TelegramBot extends TelegramWebhookBot implements ICommandRegistry {

    private CommandRegistry commandRegistry;
    private final ScriptRegistry scriptRegistry;
    private final TelegramConfig telegramConfig;

    @Autowired
    private MessageService messageService; //эти 2 бина ссылаются друг на друга. Конструктор не подойдет

    public TelegramBot(TelegramConfig telegramConfig, ScriptRegistry scriptRegistry) {
        super();
        this.telegramConfig = telegramConfig;
        commandRegistry = new CommandRegistry(true, getBotUsername());
        this.scriptRegistry = scriptRegistry;
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        try {
            if (update != null) {
                if (update.getMessage() != null && update.getMessage().isCommand()) {
                    commandRegistry.executeCommand(this, update.getMessage());
                } else {
                    if (update.hasCallbackQuery()) {
                        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery()
                                .setCallbackQueryId(update.getCallbackQuery().getId());
                        try {
                            executeAsync(answerCallbackQuery, new SentCallback<>() {
                                @Override
                                public void onResult(BotApiMethod<Boolean> method, Boolean response) {

                                }

                                @Override
                                public void onError(BotApiMethod<Boolean> method,
                                                    TelegramApiRequestException apiException) {
                                    log.error(apiException);
                                }

                                @Override
                                public void onException(BotApiMethod<Boolean> method, Exception exception) {
                                    log.error(exception);
                                }
                            });
                        } catch (TelegramApiException e) {
                            log.error("Send callback error", e);
                        }
                    }
                    if (!scriptRegistry.execute(this, update)) {
                        messageService.send(update);
                    }
                }
            }
        } catch (Exception e) {
            log.error(String.format("Unexpected error. Update: %s", update), e);
        }
        return null;
    }

    @Override
    public void registerDefaultAction(BiConsumer<AbsSender, Message> defaultConsumer) {
        commandRegistry.registerDefaultAction(defaultConsumer);
    }

    @Override
    public boolean register(IBotCommand botCommand) {
        return commandRegistry.register(botCommand);
    }

    @Override
    public Map<IBotCommand, Boolean> registerAll(IBotCommand... botCommands) {
        return commandRegistry.registerAll(botCommands);
    }

    @Override
    public boolean deregister(IBotCommand botCommand) {
        return commandRegistry.deregister(botCommand);
    }

    @Override
    public Map<IBotCommand, Boolean> deregisterAll(IBotCommand... botCommands) {
        return commandRegistry.deregisterAll(botCommands);
    }

    @Override
    public Collection<IBotCommand> getRegisteredCommands() {
        return commandRegistry.getRegisteredCommands();
    }

    @Override
    public IBotCommand getRegisteredCommand(String commandIdentifier) {
        return commandRegistry.getRegisteredCommand(commandIdentifier);
    }

    @Override
    public String getBotUsername() {
        return telegramConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return telegramConfig.getToken();
    }

    @Override
    public String getBotPath() {
        return telegramConfig.getPath();
    }
}
