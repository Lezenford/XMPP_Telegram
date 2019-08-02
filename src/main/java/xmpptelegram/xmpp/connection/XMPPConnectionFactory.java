package xmpptelegram.xmpp.connection;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public class XMPPConnectionFactory {

    @Lookup
    public XMPPConnection createConnection() {
        return null;
    }
}
