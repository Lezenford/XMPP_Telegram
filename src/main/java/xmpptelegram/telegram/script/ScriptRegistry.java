package xmpptelegram.telegram.script;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import xmpptelegram.telegram.script.model.BotScript;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ScriptRegistry {
    private Map<User, Map<Long, BotScript>> map = new ConcurrentHashMap<>();

    public void register(AbsSender absSender, User user, Chat chat, BotScript botScript) {
        Map<Long, BotScript> chatBotScriptMap = map.get(user);
        if (chatBotScriptMap == null) {
            chatBotScriptMap = new ConcurrentHashMap<>();
            map.putIfAbsent(user, chatBotScriptMap);
            chatBotScriptMap = map.get(user);
        }
        chatBotScriptMap.put(chat.getId(), botScript);
        botScript.init(absSender, user, chat);
    }

    public boolean execute(AbsSender absSender, Update update) {
        User user;
        Long chatId;
        if (update.hasCallbackQuery()) {
            user = update.getCallbackQuery().getFrom();
            chatId = update.getCallbackQuery().getMessage().getChat().getId();
        } else {
            if (update.getMessage() != null) {
                user = update.getMessage().getFrom();
                chatId = update.getMessage().getChatId();
            } else {
                return false;
            }
        }
        Map<Long, BotScript> chatBotScriptMap = map.get(user);
        if (chatBotScriptMap != null) {
            BotScript botScript = chatBotScriptMap.get(chatId);
            if (botScript != null) {
                if (botScript.isFinished()) {
                    chatBotScriptMap.remove(chatId); //If script has finished - remove from map
                } else {
                    botScript.execute(absSender, update);
                    return true;
                }
            }
        }
        return false;
    }
}