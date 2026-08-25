package me.sirius.sdamage.util;

import me.sirius.core.text.Text;
import me.sirius.sdamage.SPvP;
import org.bukkit.command.CommandSender;

public final class Messages {

    private final SPvP plugin;
    private String prefix = "";

    public Messages(SPvP plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.prefix = plugin.config().raw().getString("messages.prefix", "");
    }

    public String get(String key) {
        return prefix + plugin.config().raw().getString("messages." + key, key);
    }

    public void send(CommandSender to, String key) {
        Text.send(to, get(key));
    }

    public void sendRaw(CommandSender to, String message) {
        Text.send(to, prefix + message);
    }
}
