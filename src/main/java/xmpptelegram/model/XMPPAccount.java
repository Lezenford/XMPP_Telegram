package xmpptelegram.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@SuppressWarnings("JpaQlInspection")
@NamedQueries({
        @NamedQuery(name = XMPPAccount.ALL, query = "SELECT x FROM XMPPAccount x"),
        @NamedQuery(name = XMPPAccount.GET_BY_LOGIN_AND_SERVER, query = "SELECT x FROM XMPPAccount x WHERE x.server=:server AND x" +
                ".login=:login"),
        @NamedQuery(name = XMPPAccount.GET_ALL_BY_USER, query = "SELECT x FROM XMPPAccount x JOIN FETCH x.telegramUser WHERE x" +
                ".telegramUser.id = :telegramUserId")
})
@Entity
@Data
@NoArgsConstructor
@Table(name = "xmpp_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"login", "server"}, name =
                "xmpp_accounts_login_server_index")})
public class XMPPAccount {
    public static final String ALL = "XMPPAccount.getAllConnections";
    public static final String GET_BY_LOGIN_AND_SERVER = "XMPPAccount.getByLoginAndServer";
    public static final String GET_ALL_BY_USER = "XMPPAccount.getAllByUser";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "server")
    @NotNull
    @NotBlank
    private String server;

    @Column(name = "login")
    @NotNull
    @NotBlank
    private String login;

    @Column(name = "password")
    @NotNull
    @NotBlank
    private String password;

    @Column(name = "port", columnDefinition = "5222")
    @NotNull
    private int port;

    @Column(name = "active", columnDefinition = "1")
    private boolean active = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "telegramuser", nullable = false)
    @NotNull
    private TelegramUser telegramUser;

    public XMPPAccount(String server, String login, String password) {
        this.server = server;
        this.login = login;
        this.password = password;
        port = 5222;
    }

    public XMPPAccount(String server, String login, String password, int port) {
        this.server = server;
        this.login = login;
        this.password = password;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XMPPAccount account = (XMPPAccount) o;
        return Objects.equals(server, account.server) &&
                Objects.equals(login, account.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, login);
    }
}
