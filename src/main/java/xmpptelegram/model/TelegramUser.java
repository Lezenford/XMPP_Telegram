package xmpptelegram.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "telegram_users")
public class TelegramUser {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Column(name = "username")
    private String name;

    @Column(name = "defaultchat")
    private long defaultChat;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "telegramUser", cascade = CascadeType.ALL)
    private Set<XMPPAccount> accounts = new HashSet<>();

    public TelegramUser(int id, String name) {
        this.id = id;
        this.name = name;
        defaultChat = id;
    }
}
