package xmpptelegram.repository.jpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.XMPPAccount;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Slf4j
@Repository
@Transactional(readOnly = true)
public class ChatMapRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ChatMap> getAll() {
        return null;
    }

    public ChatMap getByUserAndAccountAndContact(XMPPAccount account, String contact) {
        try {
            return entityManager.createNamedQuery(ChatMap.GET_BY_ACCOUNT_CONTACT, ChatMap.class)
                                .setParameter("xmppAccount", account.getId())
                                .setParameter("xmppContact", contact)
                                .getSingleResult();
        } catch (NoResultException e) {
            log.debug(String.format("Empty chatmap data for XMPPAccount: %s, contact: %s", account.getLogin() + "@" + account.getServer(),
                    contact));
            return null;
        }
    }

    @Transactional
    public void create(ChatMap chatMap) throws Exception {
        try {
            entityManager.persist(chatMap);
        } catch (Exception e) {
            log.error(String.format("Error adding chatmap! Data: %s", chatMap.toString()), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public ChatMap update(ChatMap chatMap) {
        return entityManager.merge(chatMap);
    }

    public ChatMap getByChatId(long chatId) {
        try {
            return entityManager.createNamedQuery(ChatMap.GET_BY_CHATID, ChatMap.class)
                                .setParameter("chatId", chatId)
                                .getSingleResult();
        } catch (NoResultException e) {
            log.debug(String.format("Empty chatmap data for chatId: %d", chatId), e.getMessage());
            return null;
        }
    }

    @Transactional
    public void delete(ChatMap chatMap) {
        try {
            entityManager.remove(chatMap);
        } catch (Exception e) {
            log.warn(String.format("Error deleting chatmap: %s", chatMap.toString()), e.getMessage());
        }
    }

    public ChatMap sendToTelegram(XMPPAccount xmppAccount, String contact) {
        try {
            return entityManager.createNamedQuery(ChatMap.GET_BY_ACCOUNT_CONTACT, ChatMap.class)
                                .setParameter("xmppAccount", xmppAccount.getId())
                                .setParameter("xmppContact", contact)
                                .getSingleResult();
        } catch (NoResultException e) {
            log.warn(String.format("Empty chatmap data for XMPPAccount: %s, contact: %s",
                    xmppAccount.getLogin() + "@" + xmppAccount.getServer(), contact), e.getMessage());
            return null;
        }
    }
}
