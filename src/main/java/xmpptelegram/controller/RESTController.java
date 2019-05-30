package xmpptelegram.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;
import xmpptelegram.bot.TelegramBot;
import xmpptelegram.bot.XMPPBot;
import xmpptelegram.repository.jpa.MessageRepository;
import xmpptelegram.service.ChatMapService;

@Slf4j
@RestController
public class RESTController {

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatMapService chatMapService;

    @Autowired
    private XMPPBot xmppBot;

    @RequestMapping("/")
    public String test() {
        return null;
    }

    @RequestMapping("/secured")
    public String secured() {
        return "HTTPS Enable";
    }

//    @RequestMapping("/stop")
//    public String stop(){
//        xmppBot.stop();
//        return "XMPP server stoped";
//    }
//    @RequestMapping("/start")
//    public String start(){
//        xmppBot.start();
//        return "XMPP server started";
//    }

    @RequestMapping(value = "/${telegram.token}", method = RequestMethod.POST)
    @ResponseBody
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }
}
