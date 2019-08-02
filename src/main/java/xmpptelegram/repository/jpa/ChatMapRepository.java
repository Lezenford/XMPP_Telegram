package xmpptelegram.repository.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xmpptelegram.model.ChatMap;
import xmpptelegram.model.XMPPAccount;

import javax.persistence.EntityManager;

@Log4j2
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMapRepository {

    private final EntityManager entityManager;

    public ChatMap get(XMPPAccount account, String contact) {
        try {
            return entityManager
                    .createQuery("SELECT c FROM ChatMap c JOIN FETCH c.xmppAccount WHERE " +
                            "c.xmppAccount.id=:xmppAccount AND c.xmppContact=:xmppContact", ChatMap.class)
                    .setParameter("xmppAccount", account.getId())
                    .setParameter("xmppContact", contact)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public ChatMap get(long chatId) {
        try {
            return entityManager.createQuery("SELECT c FROM ChatMap c WHERE c.chatId=:chatId", ChatMap.class)
                                .setParameter("chatId", chatId)
                                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public boolean create(ChatMap chatMap) {
        try {
            entityManager.persist(chatMap);
            return true;
        } catch (Exception e) {
            log.error(String.format("Can't create ChatMap in database! ChatMap: %s", chatMap.toString()), e);
            return false;
        }
    }

    @Transactional
    public ChatMap update(ChatMap chatMap) {
        try {
            return entityManager.merge(chatMap);
        } catch (Exception e) {
            log.error(String.format("Can't update ChatMap in database! ChatMap: %s", chatMap.toString()), e);
            return null;
        }
    }


    @Transactional
    public boolean delete(ChatMap chatMap) {
        try {
            entityManager.remove(chatMap);
            return true;
        } catch (Exception e) {
            log.error(String.format("Can't remove ChatMap from database! ChatMap: %s", chatMap.toString()), e);
            return false;
        }
    }
}
