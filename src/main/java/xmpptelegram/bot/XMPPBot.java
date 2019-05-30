package xmpptelegram.bot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xmpptelegram.model.TransferMessage;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.model.XMPPConnection;
import xmpptelegram.service.MessageService;
import xmpptelegram.service.XMPPAccountService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@NoArgsConstructor
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class XMPPBot {

    private final ConcurrentMap<XMPPAccount, XMPPConnection> connections = new ConcurrentHashMap<>();

    public static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @Autowired
    private XMPPAccountService accountService;

    @Getter
    @Autowired
    private MessageService messageService;

    public void start() {
        log.debug("XMPPBot is starting");
        if (connections.size() > 0) {
            stop();
        }
        List<XMPPAccount> accounts = accountService.getAll();
        for (XMPPAccount account : accounts) {
            connections.put(account, new XMPPConnection(account, this).createConnection());
        }
        log.info("XMPPBot started");
    }

    public void stop() {
        connections.forEach((XMPPAccount k, XMPPConnection v) -> connections.remove(k).closeConnection());
    }


    public void disconnectAccount(XMPPAccount account) {
        connections.remove(account).closeConnection();
    }

    public void connectAccount(XMPPAccount account) {
        connections.put(account, new XMPPConnection(account, this).createConnection());
    }

    //Сообщения из Telegram в XMPP
    public boolean sendXMPPMessage(TransferMessage transferMessage) {
        try {
            return connections.get(transferMessage.getChatMap().getXmppAccount()).sendMessage(transferMessage);
        } catch (NullPointerException e) {
            log.error(String.format("Can't send message to that XMPPAccount! XMPPAccount doesn't have connection! XMPPAccount: %s",
                    transferMessage.getChatMap().getXmppAccount().toString()));
            return false;
        }
    }

    public String checkStatus(XMPPAccount account) {
        try {
            return connections.get(account).getStatus();
        } catch (NullPointerException e) {
            return "Нет аккаунта";
        }
    }

    public void checkStatus(String server, String login) {
        checkStatus(accountService.get(server, login));
    }

    @Scheduled(fixedDelay = 120_000L, initialDelay = 180_000L)
    private void checkAllConnections() {
        connections.forEach((XMPPAccount k, XMPPConnection v) -> {
            if (!v.isConnected()) {
                v.createConnection();
            }
        });
    }
}
