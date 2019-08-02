package xmpptelegram.telegram.script.factory;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import xmpptelegram.telegram.script.model.AddAccountScript;
import xmpptelegram.telegram.script.model.AddGroupScript;
import xmpptelegram.telegram.script.model.UpdateAccountScript;

@Component
public class ScriptFactory {
    @Lookup
    public AddAccountScript getAddAccountScript() {
        return null;
    }

    @Lookup
    public UpdateAccountScript getUpdateAccountScript() {
        return null;
    }

    @Lookup
    public AddGroupScript getAddGroupScript() {
        return null;
    }

}
