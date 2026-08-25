package me.sirius.sdamage.config;

import me.sirius.sdamage.SPvP;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigManager {

    private static final int CURRENT_VERSION = 3;

    private final SPvP plugin;
    private FileConfiguration config;

    public ConfigManager(SPvP plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        checkVersion();
    }

    private void checkVersion() {
        int found = config.getInt("config-version", 1);
        if (found >= CURRENT_VERSION) {
            return;
        }
        plugin.getLogger().warning("config.yml desactualizado (v" + found + " -> v"
                + CURRENT_VERSION + "). Renombra tu config.yml a config.old.yml para regenerar "
                + "los valores nuevos; mientras tanto se usan los defaults para lo que falte.");
    }

    public FileConfiguration raw() {
        return config;
    }
}
