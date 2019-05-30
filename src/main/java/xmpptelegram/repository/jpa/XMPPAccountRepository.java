package xmpptelegram.repository.jpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xmpptelegram.model.XMPPAccount;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Slf4j
@Repository
@Transactional(readOnly = true)
public class XMPPAccountRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<XMPPAccount> getAll() {
        return entityManager.createNamedQuery(XMPPAccount.ALL, XMPPAccount.class)
                            .getResultList();
    }

    public XMPPAccount get(String server, String login) {
        try {
            return entityManager.createNamedQuery(XMPPAccount.GET_BY_LOGIN_AND_SERVER, XMPPAccount.class)
                                .setParameter("server", server)
                                .setParameter("login", login)
                                .getSingleResult();
        } catch (NoResultException e) {
            log.warn(String.format("User not found login: %s, server %s", login, server), e.getMessage());
            return null;
        }
    }

    public int delete(XMPPAccount account) {
        return 0;
    }

    @Transactional
    public XMPPAccount update(XMPPAccount account) {
        try {
            return entityManager.merge(account);
        } catch (Exception e) {
            log.warn(String.format("Error updating XMPP-account! Server: %s, login: %s", account.getServer(), account.getLogin()),
                    e.getMessage());
            return null;
        }
    }

    @Transactional
    public void create(XMPPAccount account) {
        entityManager.persist(account);
    }

    public XMPPAccount getById(int id) {
        return entityManager.find(XMPPAccount.class, id);
    }

    public List<XMPPAccount> getAllByUser(int userId) {
        try {
            return entityManager.createNamedQuery(XMPPAccount.GET_ALL_BY_USER, XMPPAccount.class)
                                .setParameter("telegramUserId", userId)
                                .getResultList();
        } catch (NoResultException e) {
            log.warn(String.format("Users not found telegram user id: %s", userId), e.getMessage());
            return null;
        }
    }
}
