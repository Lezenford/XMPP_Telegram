package xmpptelegram.repository.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import xmpptelegram.model.UnsentMessage;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageRepository {

    private final EntityManager entityManager;

    @Transactional
    public boolean create(UnsentMessage message) {
        try {
            entityManager.persist(message);
            return true;
        } catch (Exception e) {
            log.error(String.format("Can't create UnsentMessage in database! Message: %s", message.toString()), e);
            return false;
        }
    }

    @Transactional
    public boolean delete(UnsentMessage message) {
        try {
            entityManager.remove(message);
            return true;
        } catch (Exception e) {
            log.error(String.format("Can't remove UnsentMessage in database! Message: %s", message.toString()), e);
            return false;
        }
    }

    public List<UnsentMessage> getAll() {
        try {
            return entityManager.createQuery("SELECT m FROM UnsentMessage m ORDER BY m.id", UnsentMessage.class)
                                .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }
}
