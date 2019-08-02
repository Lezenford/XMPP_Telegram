package xmpptelegram.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "xmpp_accounts", uniqueConstraints = {@UniqueConstraint(columnNames = {"login", "server"},
        name = "xmpp_accounts_login_server_index")})
public class XMPPAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "server", nullable = false)
    @EqualsAndHashCode.Exclude
    private String server;

    @Column(name = "login", nullable = false)
    @EqualsAndHashCode.Exclude
    private String login;

    @Column(name = "password", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private String password;

    @Column(name = "port", nullable = false)
    @ColumnDefault("5222")
    @EqualsAndHashCode.Exclude
    private int port = 5222;

    @Column(name = "active", nullable = false)
    @ColumnDefault("TRUE")
    @EqualsAndHashCode.Exclude
    private boolean active = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "telegramuser", nullable = false)
    @EqualsAndHashCode.Exclude
    private TelegramUser telegramUser;
}
