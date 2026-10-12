package me.sirius.sdamage.module.knockback;

import me.sirius.sdamage.SPvP;
import me.sirius.sdamage.module.AbstractModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class KnockbackModule extends AbstractModule {

    private double horizontal;
    private double vertical;
    private double verticalLimit;
    private double sprintHorizontal;
    private double sprintVertical;
    private double friction;
    private double enchantHorizontal;
    private boolean playersOnly;

    public KnockbackModule(SPvP plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "knockback";
    }

    @Override
    protected void onEnable() {
        ConfigurationSection s = section();
        this.horizontal = s.getDouble("horizontal", 0.4);
        this.vertical = s.getDouble("vertical", 0.4);
        this.verticalLimit = s.getDouble("vertical-limit", 0.4);
        this.sprintHorizontal = s.getDouble("sprint-horizontal", 0.4);
        this.sprintVertical = s.getDouble("sprint-vertical", 0.0);
        this.friction = Math.max(0.0001, s.getDouble("friction", 2.0));
        this.enchantHorizontal = s.getDouble("enchant-horizontal", 0.5);
        this.playersOnly = s.getBoolean("players-only", true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (playersOnly && !(victim instanceof Player)) {
            return;
        }
        if (!(event.getDamager() instanceof LivingEntity attacker) || attacker.equals(victim)) {
            return;
        }
        if (!plugin.worlds().isEnabled(victim.getWorld().getName())) {
            return;
        }

        // Direccion horizontal atacante -> victima.
        Vector dir = victim.getLocation().toVector().subtract(attacker.getLocation().toVector());
        dir.setY(0);
        if (dir.lengthSquared() > 1.0e-6) {
            dir.normalize();
        } else {
            dir = attacker.getLocation().getDirection().setY(0);
            // Mirando recto arriba o abajo la direccion horizontal es cero, y
            // normalizarla da NaN: Paper rechaza esa velocidad con una excepcion.
            if (dir.lengthSquared() > 1.0e-6) {
                dir.normalize();
            }
        }

        double h = horizontal + enchantLevel(attacker) * enchantHorizontal;
        double v = vertical;
        if (attacker instanceof Player p && p.isSprinting()) {
            h += sprintHorizontal;
            v += sprintVertical;
        }

        Vector current = victim.getVelocity();
        double x = current.getX() / friction + dir.getX() * h;
        double z = current.getZ() / friction + dir.getZ() * h;
        double y = current.getY() / friction + v;
        if (y > verticalLimit) {
            y = verticalLimit;
        }
        final Vector result = new Vector(x, y, z);

        // Se aplica un tick despues para sobrescribir el KB vanilla del mismo golpe.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (victim.isValid()) {
                victim.setVelocity(result);
            }
        });
    }

    private int enchantLevel(LivingEntity attacker) {
        if (attacker.getEquipment() == null) {
            return 0;
        }
        ItemStack hand = attacker.getEquipment().getItemInMainHand();
        try {
            return hand.getEnchantmentLevel(Enchantment.KNOCKBACK);
        } catch (Throwable ignored) {
            return 0;
        }
    }
}
