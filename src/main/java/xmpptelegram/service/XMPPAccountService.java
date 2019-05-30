package xmpptelegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xmpptelegram.model.TelegramUser;
import xmpptelegram.model.XMPPAccount;
import xmpptelegram.repository.jpa.XMPPAccountRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class XMPPAccountService {

    @Autowired
    private XMPPAccountRepository repository;

    public List<XMPPAccount> getAll() {
        return repository.getAll();
    }

    public XMPPAccount get(String server, String login) {
        return repository.get(server, login);
    }

    public boolean delete(XMPPAccount account) {
        return repository.delete(account) > 0;
    }

    public XMPPAccount update(XMPPAccount account) {
        return repository.update(account);
    }

    public boolean create(TelegramUser user, String server, String login, String password, int port) {
        if (repository.get(server, login) == null) {
            XMPPAccount account = new XMPPAccount(server, login, password, port);
            account.setTelegramUser(user);
            repository.create(account);
            return true;
        } else return false;
    }

    public List<XMPPAccount> getAllByUser(TelegramUser user) {
        List<XMPPAccount> result = repository.getAllByUser(user.getId());
        if (result == null)
            result = new ArrayList<>();
        return result;
    }

    public XMPPAccount getById(int id) {
        return repository.getById(id);
    }
}
