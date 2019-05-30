package xmpptelegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.repository.jpa.ChatMapRepository;

import java.util.List;

@Slf4j
@Service
public class ChatMapService {

    @Autowired
    private ChatMapRepository repository;

    public List<ChatMap> getAll() {
        return repository.getAll();
    }

    public ChatMap sendToTelegram(XMPPAccount xmppAccount, String contact) {
        return repository.sendToTelegram(xmppAccount, contact);
    }

    public boolean create(XMPPAccount account, long chatId, String contact) {
        if (getByAccountAndContact(account, contact) == null) {
            try {
                ChatMap map = new ChatMap(chatId, account, contact);
                repository.create(map);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else return false;
    }

    public ChatMap getByAccountAndContact(XMPPAccount account, String contact) {
        return repository.getByUserAndAccountAndContact(account, contact);
    }

    public ChatMap getByChatId(long chatId) {
        return repository.getByChatId(chatId);
    }

    public boolean delete(long chatId) {
        ChatMap chatMap = repository.getByChatId(chatId);
        if (chatMap == null) {
            return false;
        } else {
            try {
                repository.delete(chatMap);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public boolean delete(ChatMap chatMap) {
        if (chatMap == null)
            return false;
        else {
            try {
                repository.delete(chatMap);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
