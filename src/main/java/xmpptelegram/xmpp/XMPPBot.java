package xmpptelegram.xmpp;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.repository.jpa.XMPPAccountRepository;
import xmpptelegram.xmpp.connection.XMPPConnection;
import xmpptelegram.xmpp.connection.XMPPConnectionFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@RequiredArgsConstructor
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class XMPPBot {

    private final ConcurrentMap<XMPPAccount, XMPPConnection> connections = new ConcurrentHashMap<>();

    private final XMPPAccountRepository accountRepository;

    private final XMPPConnectionFactory factory;

    public void start() {
        log.debug("XMPPBot is starting");
        if (connections.size() > 0) {
            stop();
        }
        List<XMPPAccount> accounts = accountRepository.getAll();
        for (XMPPAccount account : accounts) {
            if (account.isActive()) {
                connections.put(account, factory.createConnection().connect(account));
            }
        }
        log.info("XMPPBot started");
    }

    private void stop() {
        connections.forEach((k, v) -> connections.remove(k).disconnect());
    }

    public void disconnectAccount(XMPPAccount account) {
        if (connections.containsKey(account)) {
            connections.remove(account).disconnect();
        }
    }

    public void connectAccount(XMPPAccount account) {
        if (connections.containsKey(account)) {
            connections.remove(account).disconnect();
        }
        connections.put(account, factory.createConnection().connect(account));
    }

    //Сообщения из Telegram в XMPP
    public boolean sendMessage(ChatMap map, String text) {
        try {
            return connections.get(map.getXmppAccount()).sendMessage(map, text);
        } catch (NullPointerException e) {
            log.error(String
                    .format("Can't send message to that XMPPAccount! XMPPAccount doesn't have connection! " +
                                    "XMPPAccount: %s",
                            map.toString()));
            return false;
        }
    }

    public boolean checkConnection(XMPPAccount account) {
        if (connections.containsKey(account)) {
            return connections.get(account).isConnected();
        } else {
            return false;
        }
    }

    public int getConnectedAccountCount() {
        AtomicInteger count = new AtomicInteger();
        connections.forEach((account, xmppConnection) -> {
            if (xmppConnection.isConnected()) {
                count.incrementAndGet();
            }
        });
        return count.get();
    }

    @Scheduled(fixedDelay = 120_000L, initialDelay = 180_000L)
    private void checkAllConnections() {
        connections.forEach((k, v) -> {
            if (!v.isConnected()) {
                v.connect(k);
            }
        });
    }
}
