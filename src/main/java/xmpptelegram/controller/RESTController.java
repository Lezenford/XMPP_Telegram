package xmpptelegram.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import xmpptelegram.telegram.TelegramBot;

@Log4j2
@RestController
@RequiredArgsConstructor
public class RESTController {

    private final TelegramBot telegramBot;

    @RequestMapping("/")
    public String test() {
        return null;
    }

    @RequestMapping("/secured")
    public String secured() {
        return "HTTPS Enable";
    }

    @RequestMapping(value = "/${telegram.token}", method = RequestMethod.POST)
    @ResponseBody
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }
}
