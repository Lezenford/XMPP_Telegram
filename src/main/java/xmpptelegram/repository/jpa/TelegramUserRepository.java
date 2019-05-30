package xmpptelegram.repository.jpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xmpptelegram.model.TelegramUser;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Slf4j
@Repository
@Transactional(readOnly = true)
public class TelegramUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<TelegramUser> getAll() {
        return entityManager.createNamedQuery(TelegramUser.ALL, TelegramUser.class)
                            .getResultList();
    }

    public TelegramUser getById(int id) {
        try {
            return entityManager.createNamedQuery(TelegramUser.GET_BY_ID, TelegramUser.class)
                                .setParameter("id", id)
                                .getSingleResult();
        } catch (NoResultException e) {
            log.warn(String.format("User not found %d", id), e.getMessage());
            return null;
        }
    }

    @Transactional
    public void delete(TelegramUser user) throws Exception {
        try {
            entityManager.remove(user);
        } catch (Exception e) {
            log.error(String.format("Error deleting Telegram-account! id: %s", user.getId()), e.getMessage());
        }
    }

    @Transactional
    public void update(TelegramUser user) throws Exception {
        try {
            entityManager.merge(user);
        } catch (Exception e) {
            log.error(String.format("Error updating Telegram-account! id: %d, username: %s", user.getId(), user.getName()), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void create(int id, String username) throws Exception {
        try {
            TelegramUser user = new TelegramUser(id, username);
            entityManager.persist(user);
        } catch (Exception e) {
            log.error(String.format("Error adding Telegram-account! id: %d, username: %s", id, username), e.getMessage());
            throw e;
        }
    }
}
