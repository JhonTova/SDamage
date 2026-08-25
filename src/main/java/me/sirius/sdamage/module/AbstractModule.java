package me.sirius.sdamage.module;

import me.sirius.sdamage.SPvP;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class AbstractModule implements Module, Listener {

    protected final SPvP plugin;
    private boolean active;

    protected AbstractModule(SPvP plugin) {
        this.plugin = plugin;
    }

    protected ConfigurationSection section() {
        ConfigurationSection s = plugin.config().raw().getConfigurationSection(id());
        return s != null ? s : new MemoryConfiguration();
    }

    @Override
    public boolean isConfigEnabled() {
        return section().getBoolean("enabled", true);
    }

    @Override
    public final void enable() {
        if (active) {
            return;
        }
        onEnable();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        active = true;
    }

    @Override
    public final void disable() {
        if (!active) {
            return;
        }
        HandlerList.unregisterAll(this);
        onDisable();
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}
