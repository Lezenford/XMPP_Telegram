package xmpptelegram.service;

import org.springframework.stereotype.Component;
import xmpptelegram.model.XMPPAccount;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UnmappedChatQueries {
    private Map<XMPPAccount, Set<String>> queries = new ConcurrentHashMap<>();

    public Set<String> getAccountChats(XMPPAccount account) {
        return queries.getOrDefault(account, new HashSet<>());
    }

    void addAccountChat(XMPPAccount account, String contact) {
        Set<String> contacts = Collections.synchronizedSet(new HashSet<>());
        queries.putIfAbsent(account, contacts);
        contacts = queries.get(account);
        contacts.add(contact);
    }

    public void removeAccountChat(XMPPAccount account, String contact) {
        Set<String> contacts = queries.get(account);
        if (contacts != null && contact != null) {
            contacts.remove(contact);
        }
    }
}
