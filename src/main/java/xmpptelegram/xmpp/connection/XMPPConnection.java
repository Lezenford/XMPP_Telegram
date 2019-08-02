package xmpptelegram.xmpp.connection;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.java7.XmppHostnameVerifier;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.service.MessageService;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

@Log4j2
@Component
@RequiredArgsConstructor
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class XMPPConnection {

    private final MessageService messageService;
    private final ExecutorService threadPool;
    private final SSLContext sslContext;

    private AbstractXMPPConnection connection;


    /**
     * Создание подключения
     */
    public XMPPConnection connect(XMPPAccount account) {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
        try {
            XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration
                    .builder()
                    .setXmppDomain(JidCreate.domainBareFrom(account.getServer()))
                    .setPort(account.getPort())
                    .setHost(account.getServer())
                    .setHostAddress(InetAddress.getByName(account.getServer()))
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                    .setCustomSSLContext(sslContext)
                    .setHostnameVerifier(new XmppHostnameVerifier())
                    .build();
            connection = new XMPPTCPConnection(configuration);
            connection.addConnectionListener(new ConnectionListener() {
                @Override
                public void connected(org.jivesoftware.smack.XMPPConnection connection) {
                }

                @Override
                public void authenticated(org.jivesoftware.smack.XMPPConnection connection, boolean resumed) {
                    threadPool.execute(() -> messageService.send(account, null,
                            String.format("%s@%s: В сети", account.getLogin(), account.getServer())));
                }

                @Override
                public void connectionClosed() {
                    threadPool.execute(() -> messageService.send(account, null,
                            String.format("%s@%s: Не в сети", account.getLogin(), account.getServer())));
                }

                @Override
                public void connectionClosedOnError(Exception e) {
                    log.error(String.format("XMPP connection error. Account: %s@%s",
                            account.getLogin(), account.getServer()), e);
                    connectionClosed();
                }
            });
            ChatManager.getInstanceFor(connection)
                       .addIncomingListener((EntityBareJid from, Message message, Chat chat) -> {
                           if (message.getType().equals(Message.Type.chat) && message.getBody() != null) {
                               threadPool.execute(() -> messageService
                                       .send(account, message.getFrom().asEntityBareJidIfPossible().toString(),
                                               message.getBody()));
                           }
                       });
            try {
                connection.connect();
                try {
                    connection.login(account.getLogin(), account.getPassword());
                } catch (IOException | InterruptedException | SmackException | XMPPException e) {
                    log.error("Login error", e);
                    threadPool.execute(() -> messageService.send(account, null,
                            String.format("Ошибка авторизации. Подключение не удалось. Аккаунт: %s@%s",
                                    account.getLogin(), account.getServer())));
                }
            } catch (IOException | InterruptedException | XMPPException | SmackException e) {
                log.error("Server connection error", e);
                threadPool.execute(() -> messageService.send(account, null,
                        String.format("Ошибка подключения к серверу. Аккаунт: %s@%s",
                                account.getLogin(), account.getServer())));
            }
        } catch (UnknownHostException e) {
            log.error(e);
            threadPool.execute(() -> messageService.send(account, null,
                    String.format("Адрес сервера для аккаунта %s@%s не найден. Подключение прервано.",
                            account.getLogin(), account.getServer())));
        } catch (XmppStringprepException e) {
            log.error("XMPPTCPConnectionConfiguration error", e);
            threadPool.execute(() -> messageService.send(account, null,
                    String.format("Ошибка подключения аккаунта %s@%s", account.getLogin(), account.getServer())));
        }
        return this;
    }

    /**
     * Закрытие подключения
     */
    public void disconnect() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public boolean sendMessage(ChatMap map, String text) {
        try {
            ChatManager.getInstanceFor(connection)
                       .chatWith(JidCreate.entityBareFrom(map.getXmppContact()))
                       .send(text);
            return true;
        } catch (SmackException.NotConnectedException | InterruptedException | XmppStringprepException e) {
            log.error(String.format("Can't send message to XMPP! Account: %s@%s, contact: %s, text: %s",
                    map.getXmppAccount().getLogin(), map.getXmppAccount().getServer(), map.getXmppContact(), text));
            return false;
        }
    }
}