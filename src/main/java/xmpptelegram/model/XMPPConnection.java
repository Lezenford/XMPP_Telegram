package xmpptelegram.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.java7.XmppHostnameVerifier;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import xmpptelegram.bot.XMPPBot;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
public class XMPPConnection {

    @Getter
    private final String server;

    @Getter
    private final String login;

    @Getter
    private AbstractXMPPConnection connection;

    private final String password;
    private final int port;
    private final XMPPBot controller;
    private XMPPTCPConnectionConfiguration configuration;
    private final SSLSetting sslSetting = new SSLSetting();

    public XMPPConnection(XMPPAccount xmppAccount, XMPPBot controller) {
        server = xmppAccount.getServer();
        login = xmppAccount.getLogin();
        password = xmppAccount.getPassword();
        port = xmppAccount.getPort();
        this.controller = controller;
        connection = null;
        configuration = null;
        log.debug(String.format("New XMPPConnection by server: %s, login: %s", server, login));
    }

    private void configure() throws Exception { //собираем настройки и проверяем их на корректность. Пробрасываем исключение, если возникнет
//        SmackConfiguration.DEBUG = true; //Включает режим отладки XMPP сообщений
        configuration = XMPPTCPConnectionConfiguration.builder()
                                                      .setXmppDomain(JidCreate.domainBareFrom(server))
                                                      .setPort(port)
                                                      .setHost(server)
                                                      .setHostAddress(InetAddress.getByName(server))
                                                      .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                                                      .setCustomSSLContext(sslSetting.getSslContext()) //неизвестно, как это поведет себя
                                                      // если подключение отличается от заданного эталона - TLS
                                                      .setHostnameVerifier(sslSetting.getHostNameVerifier())
                                                      .build();
    }

    private void connect() throws Exception { //создаем подключение к серверу XMPP. Если соединение не удалось - пробрысываем исключение
        if (configuration == null) {
            configure();
        }
        connection = new XMPPTCPConnection(configuration).connect();
    }

    private void login() throws Exception {
        connection.login(login, password); //если не получилось авторизоваться, пробрасываем исключение
    }

    /*
     *Отправляет текущий статус учетной записи в Telegram
     */
    private void sendStatus() {
        XMPPBot.threadPool.execute(() -> controller.getMessageService().send(server, login,
                null, String.format("Аккаунт %s@%s: %s", login, server, getStatus())));
    }

    public String getStatus() {
        String status;
        if (isConnected()) {
            status = "Подключен";
        } else {
            status = "Не в сети";
        }
        return status;
    }

    /*
     *Создание подключения
     */
    public XMPPConnection createConnection() {
        if (connection == null || !connection.isConnected()) {
            try {
                configure();
                connect();
                login();
                ChatManager.getInstanceFor(connection).addIncomingListener((EntityBareJid from, Message message, Chat chat) -> {
                    if (message.getType().equals(Message.Type.chat) && message.getBody() != null) {
                        XMPPBot.threadPool.execute(() -> controller.getMessageService().send(server, login,
                                message.getFrom().asEntityBareJidIfPossible().toString(), message.getBody()));
                    }
                });
                connection.addConnectionListener(new AbstractConnectionListener() {
                    @Override
                    public void connectionClosedOnError(Exception e) {
                        XMPPBot.threadPool.execute(() -> controller.getMessageService().send(server, login,
                                null, String.format("Не удается подключиться к серверу! Аккаунт %s@%s: %s", login, server, getStatus())));
                        super.connectionClosedOnError(e);
                    }

                    @Override
                    public void connectionClosed() {
                        super.connectionClosed();
                        sendStatus();
                    }
                });
                sendStatus();
            } catch (Exception e) {
                closeConnection();
                XMPPBot.threadPool.execute(() -> controller.getMessageService().send(server, login,
                        null, String.format("Не удается подключиться к серверу! Аккаунт %s@%s: %s", login, server, getStatus())));
                log.error(String.format("Error connection XMPPAccount. Server: %s login: %s", server, login), e);
            }
        }
        return this;
    }

    public void closeConnection() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    public boolean sendMessage(TransferMessage transferMessage) {
        try {
            ChatManager.getInstanceFor(connection)
                       .chatWith(JidCreate.entityBareFrom(transferMessage.getChatMap().getXmppContact())).send(transferMessage.getText());
            return true;
        } catch (SmackException.NotConnectedException | InterruptedException | XmppStringprepException e) {
            log.warn("Can't send message to XMPP! " + transferMessage.toString());
            return false;
        }
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    private class SSLSetting {
        //делаем доверие для всех входящих сертификатов
        //TODO предусмотреть возможность выбора сертификатов
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }
                }};

        SSLContext getSslContext() {
            //создаем настройки для работы с TLS
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                return null;
            }
            return sslContext;
        }

        HostnameVerifier getHostNameVerifier() {
            //Создаем настройки для проверки хоста
            return new XmppHostnameVerifier();
        }
    }
}
