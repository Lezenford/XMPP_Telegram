package xmpptelegram.telegram.command;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.TelegramUser;
import xmpptelegram.repository.jpa.ChatMapRepository;
import xmpptelegram.repository.jpa.TelegramUserRepository;
import xmpptelegram.telegram.BotSentCallback;
import xmpptelegram.telegram.script.ScriptRegistry;
import xmpptelegram.telegram.script.factory.ScriptFactory;

@Log4j2
@Component
public class DeleteGroupCommand extends BaseBotCommand {

    private final ChatMapRepository chatMapRepository;

    public DeleteGroupCommand(ScriptRegistry scriptRegistry,
                              ScriptFactory scriptFactory, TelegramUserRepository repository,
                              ChatMapRepository chatMapRepository,
                              BotSentCallback botSentCallback) {
        super("deletegroup", "Remove current group", scriptRegistry, scriptFactory, repository, botSentCallback);
        this.chatMapRepository = chatMapRepository;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (checkUserRegister(absSender, user, chat) && checkGroupChat(absSender, user, chat)) {
            SendMessage message = new SendMessage();
            message.setChatId(chat.getId());
            TelegramUser telegramUser = repository.get(user.getId());
            ChatMap chatMap = chatMapRepository.get(chat.getId());
            if (chatMap == null) {
                message.setText("Группа не существует");
            } else if (chatMap.getXmppAccount().getTelegramUser().getId() != telegramUser.getId()) {
                message.setText("У Вас нет прав на управление данной группой");
            } else if (chatMapRepository.delete(chatMap)) {
                message.setText("Группа успешно удалена");
            } else {
                message.setText("Ошибка удаления группы!");
            }
            try {
                absSender.executeAsync(message, botSentCallback);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
}
