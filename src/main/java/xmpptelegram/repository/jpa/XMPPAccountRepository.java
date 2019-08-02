package xmpptelegram.repository.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xmpptelegram.model.TelegramUser;
import xmpptelegram.model.XMPPAccount;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class XMPPAccountRepository {

    private final EntityManager entityManager;

    public List<XMPPAccount> getAll() {
        try {
            return entityManager.createQuery("SELECT a FROM XMPPAccount a", XMPPAccount.class)
                                .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<XMPPAccount> getAll(TelegramUser user) {
        try {
            return entityManager
                    .createQuery("SELECT a FROM XMPPAccount a WHERE a.telegramUser.id = :userId", XMPPAccount.class)
                    .setParameter("userId", user.getId())
                    .getResultList();
        } catch (Exception e) {
            log.error(String.format("Can't get XMPPAccount list for existing user from database! User: %s",
                    user.toString()), e);
            return new ArrayList<>();
        }
    }

    public XMPPAccount get(String server, String login) {
        try {
            return entityManager
                    .createQuery("SELECT x FROM XMPPAccount x WHERE x.server=:server AND x.login=:login",
                            XMPPAccount.class)
                    .setParameter("server", server)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public XMPPAccount get(int id) {
        try {
            return entityManager
                    .createQuery("SELECT x FROM XMPPAccount x WHERE x.id=:id",
                            XMPPAccount.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(XMPPAccount account) {
        try {
            entityManager.remove(account);
            return true;
        } catch (Exception e) {
            log.error(String.format("Can't remove XMPPAccount from database! Account: %s", account.toString()), e);
            return false;
        }
    }

    @Transactional
    public XMPPAccount update(XMPPAccount account) {
        try {
            return entityManager.merge(account);
        } catch (Exception e) {
            log.error(String.format("Can't update XMPPAccount in database! Account: %s", account.toString()), e);
            return null;
        }
    }

    @Transactional
    public boolean create(XMPPAccount account) {
        try {
            entityManager.persist(account);
            return true;
        } catch (Exception e) {
            log.error(String.format("Can't create XMPPAccount in database! Account: %s", account.toString()), e);
            return false;
        }
    }
}
