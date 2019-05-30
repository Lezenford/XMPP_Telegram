package xmpptelegram.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@SuppressWarnings("JpaQlInspection")
@NamedQueries({
        @NamedQuery(name = ChatMap.GET_BY_ACCOUNT_CONTACT, query = "SELECT c FROM ChatMap c JOIN FETCH c.xmppAccount WHERE c.xmppAccount" +
                ".id=:xmppAccount AND c.xmppContact=:xmppContact"),
        @NamedQuery(name = ChatMap.GET_BY_CHATID, query = "SELECT c FROM ChatMap c WHERE c.chatId=:chatId")
})
@Entity
@Data
@NoArgsConstructor
@Table(name = "telegram_chats", uniqueConstraints = @UniqueConstraint(columnNames = {"chatid", "xmppaccount", "xmppcontact"}, name =
        "telegram_chats_index"))
public class ChatMap {
    public static final String GET_BY_ACCOUNT_CONTACT = "ChatMap.getByAccountAndContact";
    public static final String GET_BY_CHATID = "ChatMap.getByChatId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "chatid")
    @NotNull
    private long chatId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "xmppaccount", nullable = false)
    @NotNull
    private XMPPAccount xmppAccount;

    @Column(name = "xmppcontact")
    @NotNull
    private String xmppContact;

    public ChatMap(@NotNull @NotBlank long chatId, @NotNull XMPPAccount xmppAccount, @NotNull @NotBlank String xmppContact) {
        this.chatId = chatId;
        this.xmppAccount = xmppAccount;
        this.xmppContact = xmppContact;
    }
}
