package me.sirius.sdamage.module.ability;

import me.sirius.sdamage.SPvP;
import me.sirius.sdamage.module.AbstractModule;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class AbilityModule extends AbstractModule {

    private static final String BYPASS = "spvp.ability.bypass";

    private boolean spearEnabled;
    private int spearCooldown;
    private double spearFoodCost;   // -1 = no tocar (coste vanilla); >=0 = fijar en X puntos
    private boolean spearBlockOnCooldown;

    private boolean maceEnabled;
    private int maceCooldown;
    private double maceMinFall;

    public AbilityModule(SPvP plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "ability";
    }

    @Override
    protected void onEnable() {
        ConfigurationSection spear = section().getConfigurationSection("spear");
        this.spearEnabled = spear != null && spear.getBoolean("enabled", true);
        this.spearCooldown = spear != null ? ticks(spear.getDouble("cooldown", 3.0)) : 0;
        this.spearFoodCost = spear != null ? spear.getDouble("food-cost", -1.0) : -1.0;
        this.spearBlockOnCooldown = spear != null && spear.getBoolean("block-on-cooldown", true);

        ConfigurationSection mace = section().getConfigurationSection("mace-smash");
        this.maceEnabled = mace != null && mace.getBoolean("enabled", true);
        this.maceCooldown = mace != null ? ticks(mace.getDouble("cooldown", 4.0)) : 0;
        this.maceMinFall = mace != null ? mace.getDouble("min-fall", 1.5) : 1.5;
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSpearJab(PlayerInteractEvent event) {
        if (!spearEnabled) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return; // el jab es un clic izquierdo
        }
        ItemStack item = event.getItem();
        if (item == null || !isLungeSpear(item)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission(BYPASS) || !plugin.worlds().isEnabled(player.getWorld().getName())) {
            return;
        }
        Material mat = item.getType();

        // En cooldown: intentar bloquear el lunge.
        if (spearBlockOnCooldown && player.getCooldown(mat) > 0) {
            event.setCancelled(true);
            return;
        }
        // Aplicar cooldown visual y coste de comida fijo.
        setCooldown(player, mat, spearCooldown);
        if (spearFoodCost >= 0) {
            overrideLungeFoodCost(player, spearFoodCost);
        }
    }

    private boolean isLungeSpear(ItemStack item) {
        if (!item.getType().name().endsWith("_SPEAR")) {
            return false;
        }
        try {
            return item.getEnchantmentLevel(Enchantment.LUNGE) > 0;
        } catch (Throwable ignored) {
            return false; // por si la version no tiene el encantamiento Lunge
        }
    }

    private void overrideLungeFoodCost(Player player, double cost) {
        final int beforeFood = player.getFoodLevel();
        final float beforeSat = player.getSaturation();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            player.setFoodLevel(beforeFood);
            player.setSaturation(beforeSat);
            drainFood(player, cost);
        }, 2L);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMaceSmash(EntityDamageByEntityEvent event) {
        if (!maceEnabled || !(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (attacker.getInventory().getItemInMainHand().getType() != Material.MACE) {
            return;
        }
        if (attacker.getFallDistance() < maceMinFall) {
            return; // no es un smash: golpe normal, no se toca
        }
        if (attacker.hasPermission(BYPASS) || !plugin.worlds().isEnabled(attacker.getWorld().getName())) {
            return;
        }
        if (attacker.getCooldown(Material.MACE) > 0) {
            event.setCancelled(true); // smash en cooldown: no impacta
            return;
        }
        setCooldown(attacker, Material.MACE, maceCooldown);
    }


    private void drainFood(Player player, double points) {
        if (points <= 0) {
            return;
        }
        float sat = player.getSaturation();
        double remaining = points;
        if (sat > 0) {
            float used = (float) Math.min(sat, remaining);
            player.setSaturation(sat - used);
            remaining -= used;
        }
        if (remaining > 0) {
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - (int) Math.ceil(remaining)));
        }
    }

    private void setCooldown(Player player, Material material, int cdTicks) {
        if (cdTicks <= 0) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.setCooldown(material, cdTicks);
            }
        });
    }

    private static int ticks(double seconds) {
        return (int) Math.round(seconds * 20.0);
    }
}
