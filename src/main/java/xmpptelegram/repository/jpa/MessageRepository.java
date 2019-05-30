package xmpptelegram.repository.jpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xmpptelegram.model.UnsentMessage;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@Transactional(readOnly = true)
public class MessageRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void create(UnsentMessage message) {
        try {
            entityManager.persist(message);
        } catch (Exception e) {
            log.error(String.format("Error adding message! Message: %s", message.toString()), e);
        }
    }

    @Transactional
    public void delete(UnsentMessage message) {
        try {
            entityManager.createNamedQuery(UnsentMessage.REMOVE)
                         .setParameter("id", message.getId()).executeUpdate();
        } catch (Exception e) {
            log.error(String.format("Error deleting message! Message: %s", message.toString()), e);
        }
    }

    public List<UnsentMessage> getAll() {
        try {
            return entityManager.createNamedQuery(UnsentMessage.GET_ALL, UnsentMessage.class)
                                .getResultList();
        } catch (NoResultException e) {
            log.error("Error getting all messages", e);
            return new ArrayList<>();
        }
    }
}
