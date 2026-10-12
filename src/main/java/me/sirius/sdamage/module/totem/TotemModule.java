package me.sirius.sdamage.module.totem;

import me.sirius.sdamage.SPvP;
import me.sirius.sdamage.module.AbstractModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TotemModule extends AbstractModule {

    private static final String BYPASS = "spvp.totem.bypass";

    private int maxStackSize;
    private long cooldownMillis;

    private final Map<UUID, Long> lastPop = new HashMap<>();

    public TotemModule(SPvP plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "totem";
    }

    @Override
    protected void onEnable() {
        this.maxStackSize = Math.max(1, Math.min(99, section().getInt("max-stack-size", 16)));
        this.cooldownMillis = Math.round(section().getDouble("cooldown", 0.0) * 1000.0);
        lastPop.clear();
        // Normaliza totems que ya esten en jugadores conectados (tras un reload).
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            normalizeInventory(p);
        }
    }

    @Override
    protected void onDisable() {
        lastPop.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        normalizeInventory(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        lastPop.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        ItemStack stack = event.getItem().getItemStack();
        if (stack.getType() == Material.TOTEM_OF_UNDYING) {
            applyStackSize(stack);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        // hand != null significa que hay un totem que se va a usar.
        if (event.getHand() == null || !(event.getEntity() instanceof Player player)) {
            return;
        }
        if (cooldownMillis <= 0 || player.hasPermission(BYPASS)
                || !plugin.worlds().isEnabled(player.getWorld().getName())) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastPop.get(player.getUniqueId());
        if (last != null && now - last < cooldownMillis) {
            event.setCancelled(true); // en cooldown: el totem no salva
            return;
        }
        lastPop.put(player.getUniqueId(), now);
    }

    private void normalizeInventory(Player player) {
        if (maxStackSize <= 1) {
            return;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                applyStackSize(item);
            }
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.getType() == Material.TOTEM_OF_UNDYING) {
            applyStackSize(off);
        }
    }

    private void applyStackSize(ItemStack stack) {
        if (maxStackSize <= 1) {
            return;
        }
        try {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && !Integer.valueOf(maxStackSize).equals(meta.getMaxStackSize())) {
                meta.setMaxStackSize(maxStackSize);
                stack.setItemMeta(meta);
            }
        } catch (Throwable ignored) {
            // Version sin soporte de max-stack-size por item: se ignora.
        }
    }
}
