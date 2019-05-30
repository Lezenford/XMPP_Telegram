package xmpptelegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xmpptelegram.model.TelegramUser;
import xmpptelegram.repository.jpa.TelegramUserRepository;

import java.util.List;

@Slf4j
@Service
public class TelegramUserService {

    @Autowired
    private TelegramUserRepository repository;

    public boolean create(int id, String name) {
        if (getById(id) == null) {
            try {
                repository.create(id, name);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else return false;
    }

    public TelegramUser update(TelegramUser user) {
        try {
            repository.update(user);
            return repository.getById(user.getId());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(int id) {
        if (getById(id) != null) {
            try {
                repository.delete(getById(id));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else return false;
    }

    public TelegramUser getById(int id) {
        return repository.getById(id);
    }

    public List<TelegramUser> getAll() {
        return repository.getAll();
    }
}
