package me.sirius.sdamage.world;

import me.sirius.sdamage.SPvP;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WorldManager {

    private final SPvP plugin;
    private final Set<String> enabledWorlds = new HashSet<>();
    private boolean allWorlds;

    public WorldManager(SPvP plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        enabledWorlds.clear();
        allWorlds = false;
        List<String> worlds = plugin.config().raw().getStringList("enabled-worlds");
        for (String w : worlds) {
            if ("*".equals(w)) {
                allWorlds = true;
            } else {
                enabledWorlds.add(w.toLowerCase());
            }
        }
    }

    public boolean isEnabled(String worldName) {
        return allWorlds || enabledWorlds.contains(worldName.toLowerCase());
    }

    public Set<String> getEnabledWorlds() {
        return Set.copyOf(enabledWorlds);
    }

    public boolean isAllWorlds() {
        return allWorlds;
    }
}
