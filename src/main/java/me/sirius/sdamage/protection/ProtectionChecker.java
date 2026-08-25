package me.sirius.sdamage.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.sirius.sdamage.SPvP;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class ProtectionChecker {

    private final SPvP plugin;
    private boolean respectZones;
    private boolean worldGuardPresent;

    public ProtectionChecker(SPvP plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.respectZones = plugin.config().raw().getBoolean("protection.respect-pvp-zones", true);
        boolean checkWg = plugin.config().raw().getBoolean("protection.check-worldguard", true);
        Plugin wg = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        this.worldGuardPresent = checkWg && wg != null && wg.isEnabled();
    }

    public boolean isPvpDenied(Player player) {
        if (!respectZones || !worldGuardPresent) {
            return false;
        }
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet regions =
                    query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
            // Sujeto null = evaluacion "global" de la flag. DENY -> PVP denegado.
            return regions.queryState(null, Flags.PVP) == StateFlag.State.DENY;
        } catch (Exception ex) {
            plugin.getLogger().warning("Fallo al consultar WorldGuard: " + ex.getMessage());
            return false;
        }
    }

    public boolean isWorldGuardPresent() {
        return worldGuardPresent;
    }
}
