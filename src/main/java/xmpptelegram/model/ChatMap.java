package xmpptelegram.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "telegram_chats", uniqueConstraints =
@UniqueConstraint(columnNames = {"chatid", "xmppaccount", "xmppcontact"}, name = "telegram_chats_index"))
public class ChatMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "chatid", nullable = false, unique = true)
    private long chatId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "xmppaccount", nullable = false)
    private XMPPAccount xmppAccount;

    @Column(name = "xmppcontact")
    private String xmppContact;
}
