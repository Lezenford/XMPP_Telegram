package xmpptelegram.bot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiConstants;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.WebhookBot;
import xmpptelegram.config.TelegramConfig;
import xmpptelegram.service.MessageService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


@Component
@Slf4j
public class TelegramBot extends DefaultAbsSender implements WebhookBot {

    private final DefaultBotOptions botOptions;

    @Autowired
    private ResourceLoader loader;

    @Autowired
    private TelegramConfig config;

    @Autowired
    private MessageService messageService;

    public TelegramBot() {
        this(ApiContext.getInstance(DefaultBotOptions.class));
    }

    public TelegramBot(DefaultBotOptions options) {
        super(options);
        this.botOptions = options;
    }

    public void init() {
        try {
            ApiContextInitializer.init();
            setWebhook(config.getPath() + config.getToken(), getCert());
        } catch (TelegramApiRequestException e) {
            log.error("Error executing setWebHook method", e);
        }
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update != null) {
            log.debug(String.format("New telegram message: %s", update.toString()));
            try {
                if (update.hasChosenInlineQuery() || update.hasInlineQuery() || update.hasCallbackQuery() || update.hasEditedMessage()) {
                    // Wrong message type
                    log.warn("MessageType doesn't support. Message: %s", update.toString());
                } else if (update.hasMessage()) {
                    log.debug("Message to bot: " + update.toString());
                    messageService.send(update);
                    // Handle message
                } else {
                    log.warn("Update doesn't contains neither ChosenInlineQuery/InlineQuery/CallbackQuery/EditedMessage/Message Update: {}",
                            update.toString());
                }
            } catch (Exception e) {
                log.error(String.format("Failed to handle incoming update. Update: %s", update.toString()), e);
            }
        }
        return null;
    }

    @Override
    public void setWebhook(String url, String publicCertificatePath) throws TelegramApiRequestException {
        log.debug("Try to setWebHook with URL " + url);
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build()) {
            String requestUrl = getBaseUrl() + SetWebhook.PATH;
            HttpPost httppost = new HttpPost(requestUrl);
            httppost.setConfig(botOptions.getRequestConfig());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody(SetWebhook.URL_FIELD, url);
            if (botOptions.getMaxWebhookConnections() != null) {
                builder.addTextBody(SetWebhook.MAXCONNECTIONS_FIELD, botOptions.getMaxWebhookConnections().toString());
            }
            if (botOptions.getAllowedUpdates() != null) {
                builder.addTextBody(SetWebhook.ALLOWEDUPDATES_FIELD, new JSONArray(botOptions.getMaxWebhookConnections()).toString());
            }
            if (publicCertificatePath != null) {
                File certificate = Files.createTempFile("temp_", ".pem").toFile();
                certificate.deleteOnExit();
                try (InputStream in = loader.getResource(publicCertificatePath).getInputStream();
                     OutputStream out = new FileOutputStream(certificate)) {
                    IOUtils.copy(in, out);
                    if (certificate.exists()) {
                        log.info("Upload webHook certificate");
                        builder.addBinaryBody(SetWebhook.CERTIFICATE_FIELD, certificate, ContentType.TEXT_PLAIN, certificate.getName());
                        builder.addTextBody("has_custom_certificate", "true");
                    }
                }
            }
            HttpEntity multipart = builder.build();
            log.debug("setWebHook HTTP body\n" + multipart.toString());
            httppost.setEntity(multipart);
            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                HttpEntity ht = response.getEntity();
                BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(responseContent);
                if (!jsonObject.getBoolean(ApiConstants.RESPONSE_FIELD_OK)) {
                    throw new TelegramApiRequestException("Error setting webhook", jsonObject);
                } else {
                    log.info("WebHook has set success");
                }
            }
        } catch (JSONException e) {
            throw new TelegramApiRequestException("Error deserializing setWebhook method response", e);
        } catch (IOException e) {
            throw new TelegramApiRequestException("Error executing setWebook method", e);
        }
    }

    public String getCert() {
        return config.getCert() == null ? "" : config.getCert();
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotPath() {
        return config.getPath();
    }
}
