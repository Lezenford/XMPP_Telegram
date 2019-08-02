package xmpptelegram.repository.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xmpptelegram.model.TelegramUser;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TelegramUserRepository {

    private final EntityManager entityManager;

    public List<TelegramUser> getAll() {
        try {
            return entityManager.createQuery("SELECT t FROM TelegramUser t", TelegramUser.class)
                                .getResultList();
        } catch (Exception e) {
            log.error("Can't get TelegramAccount list from database!", e);
            return new ArrayList<>();
        }
    }

    public TelegramUser get(int id) {
        try {
            return entityManager.createQuery("SELECT t FROM TelegramUser t WHERE t.id=:id", TelegramUser.class)
                                .setParameter("id", id)
                                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public boolean delete(TelegramUser user) {
        try {
            entityManager.remove(user);
            return true;
        } catch (Exception e) {
            log.error(String.format("Can't remove TelegramAccount from database! Account: %s", user.toString()), e);
            return false;
        }
    }

    @Transactional
    public TelegramUser update(TelegramUser user) {
        try {
            return entityManager.merge(user);
        } catch (Exception e) {
            log.error(String.format("Can't update TelegramAccount in database! Account: %s", user.toString()), e);
            return null;
        }
    }

    @Transactional
    public boolean create(TelegramUser user) {
        try {
            entityManager.persist(user);
            return true;
        } catch (Exception e) {
            log.error(String.format("Can't create TelegramAccount in database! Account: %s", user.toString()), e);
            return false;
        }
    }
}