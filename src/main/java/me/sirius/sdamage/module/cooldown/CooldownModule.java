package me.sirius.sdamage.module.cooldown;

import me.sirius.sdamage.SPvP;
import me.sirius.sdamage.module.AbstractModule;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class CooldownModule extends AbstractModule {

    private static final String BYPASS = "spvp.cooldown.bypass";

    private final Map<Material, Integer> cooldownTicks = new EnumMap<>(Material.class);

    public CooldownModule(SPvP plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "cooldown";
    }

    @Override
    protected void onEnable() {
        cooldownTicks.clear();
        ConfigurationSection items = section().getConfigurationSection("items");
        if (items == null) {
            return;
        }
        for (String key : items.getKeys(false)) {
            Material mat = Material.matchMaterial(key.toUpperCase(Locale.ROOT));
            if (mat == null) {
                plugin.getLogger().warning("[cooldown] Material desconocido: " + key);
                continue;
            }
            int ticks = (int) Math.round(items.getDouble(key) * 20.0);
            if (ticks > 0) {
                cooldownTicks.put(mat, ticks);
            }
        }
    }

    @Override
    protected void onDisable() {
        cooldownTicks.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Material type = item.getType();
        // Arco/ballesta/tridente se gestionan al disparar, no al cargar.
        if (type == Material.BOW || type == Material.CROSSBOW || type == Material.TRIDENT) {
            return;
        }
        tryApply(event.getPlayer(), type);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player && event.getBow() != null) {
            tryApply(player, event.getBow().getType());
        }
    }

    private void tryApply(Player player, Material material) {
        Integer ticks = cooldownTicks.get(material);
        if (ticks == null) {
            return;
        }
        if (!plugin.worlds().isEnabled(player.getWorld().getName())) {
            return;
        }
        if (player.hasPermission(BYPASS)) {
            return;
        }
        // Un tick despues: el uso actual se completa y el cooldown aplica al siguiente.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && player.getCooldown(material) < ticks) {
                player.setCooldown(material, ticks);
            }
        });
    }
}
