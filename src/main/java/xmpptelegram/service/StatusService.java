package xmpptelegram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import xmpptelegram.repository.jpa.MessageRepository;
import xmpptelegram.repository.jpa.XMPPAccountRepository;
import xmpptelegram.telegram.TelegramBot;
import xmpptelegram.xmpp.XMPPBot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Component
@RequiredArgsConstructor
public class StatusService {
    private final XMPPBot xmppBot;
    private final TelegramBot telegramBot;
    private final MessageRepository messageRepository;
    private final XMPPAccountRepository xmppAccountRepository;

    /**
     * Записываем в файл текущее состояние бота в формате:
     * Время | статус | количество неотправленных сообщений | количество подключенных аккаунтов | всего активных
     * аккаунтов
     */
    @Scheduled(fixedDelay = 60_000L, initialDelay = 30_000L)
    public void sendStatusToFile() {
        int messages = messageRepository.getAll().size();
        AtomicInteger activeAccounts = new AtomicInteger();
        xmppAccountRepository.getAll().forEach(account -> {
            if (account.isActive()) {
                activeAccounts.incrementAndGet();
            }
        });
        WebhookInfo webhookInfo = null;
        try {
            webhookInfo = telegramBot.getWebhookInfo();
        } catch (TelegramApiException e) {
            log.error("Can't get webhook info", e);
        }
        int connectedAccounts = xmppBot.getConnectedAccountCount();
        String result = String
                .format("%s | %s | %s | %s | %s", new Date(), webhookInfo == null || webhookInfo
                                .getPendingUpdatesCount() > 0 ?
                                "Telegram connection error" : "Telegram connection success",
                        messages, connectedAccounts, activeAccounts);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("status"), Charset
                .forName("UTF-8")))) {
            writer.write(result);
        } catch (IOException e) {
            log.error("Can't create status file", e);
        }
    }
}
