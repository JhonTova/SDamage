package me.sirius.sdamage.module.food;

import me.sirius.sdamage.SPvP;
import me.sirius.sdamage.module.AbstractModule;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class FoodModule extends AbstractModule {

    private static final String BYPASS = "spvp.food.bypass";

    private final Map<Material, double[]> restore = new EnumMap<>(Material.class);
    private final Map<Material, Double> useCost = new EnumMap<>(Material.class);

    public FoodModule(SPvP plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "food";
    }

    @Override
    protected void onEnable() {
        restore.clear();
        useCost.clear();

        ConfigurationSection restoreSec = section().getConfigurationSection("restore");
        if (restoreSec != null) {
            for (String key : restoreSec.getKeys(false)) {
                Material mat = Material.matchMaterial(key.toUpperCase(Locale.ROOT));
                ConfigurationSection s = restoreSec.getConfigurationSection(key);
                if (mat == null || s == null) {
                    warnUnknown(key);
                    continue;
                }
                restore.put(mat, new double[]{s.getDouble("hunger", 0), s.getDouble("saturation", 0)});
            }
        }

        ConfigurationSection costSec = section().getConfigurationSection("use-cost");
        if (costSec != null) {
            for (String key : costSec.getKeys(false)) {
                Material mat = Material.matchMaterial(key.toUpperCase(Locale.ROOT));
                if (mat == null) {
                    warnUnknown(key);
                    continue;
                }
                useCost.put(mat, costSec.getDouble(key));
            }
        }
    }

    @Override
    protected void onDisable() {
        restore.clear();
        useCost.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.worlds().isEnabled(player.getWorld().getName())) {
            return;
        }
        double[] r = restore.get(event.getItem().getType());
        if (r == null) {
            return;
        }
        // Capturamos el estado ANTES de que vanilla aplique su restauracion.
        final int beforeFood = player.getFoodLevel();
        final float beforeSat = player.getSaturation();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            int food = clamp(beforeFood + (int) Math.round(r[0]), 0, 20);
            player.setFoodLevel(food);
            player.setSaturation((float) Math.min(food, beforeSat + r[1]));
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Double cost = useCost.get(item.getType());
        if (cost == null || cost <= 0) {
            return;
        }
        Player player = event.getPlayer();
        if (!plugin.worlds().isEnabled(player.getWorld().getName()) || player.hasPermission(BYPASS)) {
            return;
        }
        // 4 de exhaustion = 1 punto de hambre; vanilla agota saturacion antes que hambre.
        player.setExhaustion((float) (player.getExhaustion() + cost * 4.0));
    }

    private void warnUnknown(String key) {
        plugin.getLogger().warning("[food] Material desconocido: " + key);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
