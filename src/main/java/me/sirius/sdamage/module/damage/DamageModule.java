package me.sirius.sdamage.module.damage;

import me.sirius.sdamage.SPvP;
import me.sirius.sdamage.module.AbstractModule;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public final class DamageModule extends AbstractModule {

    private final Map<String, DamageRule> rules = new HashMap<>();
    private DamageRule critical = DamageRule.disabled();

    public DamageModule(SPvP plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "damage";
    }

    @Override
    protected void onEnable() {
        rules.clear();
        ConfigurationSection sources = section().getConfigurationSection("sources");
        if (sources != null) {
            for (String key : sources.getKeys(false)) {
                rules.put(key, DamageRule.from(sources, key));
            }
        }
        ConfigurationSection crit = section().getConfigurationSection("critical");
        this.critical = crit != null && crit.getBoolean("enabled", true)
                ? new DamageRule(true, crit.getDouble("multiplier", 1.0), 0, 0, -1, -1)
                : DamageRule.disabled();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        if (!plugin.worlds().isEnabled(victim.getWorld().getName())) {
            return;
        }
        if (plugin.protection().isPvpDenied(victim)) {
            return;
        }

        boolean[] crit = {false};
        String key = (event instanceof EntityDamageByEntityEvent byEntity)
                ? resolveByEntity(byEntity, crit)
                : resolveByCause(event.getCause());

        if (key == null) {
            return;
        }
        DamageRule rule = rules.get(key);
        if (rule == null || !rule.enabled()) {
            return;
        }

        double extraMult = crit[0] ? critical.apply(1.0, 1.0) : 1.0; // critical.multiplier o 1.0
        double result = rule.apply(event.getDamage(), extraMult);
        event.setDamage(result);

        // Tope sobre el dano REAL (despues de armadura/proteccion/resistencia).
        // Escalamos el dano base hasta que el final quede en maxReal; iteramos porque
        // la reduccion por armadura no es perfectamente proporcional al dano.
        if (rule.hasMaxReal()) {
            for (int i = 0; i < 5; i++) {
                double finalDmg = event.getFinalDamage();
                if (finalDmg <= rule.maxReal() + 0.001 || finalDmg <= 0.0) {
                    break;
                }
                event.setDamage(event.getDamage() * (rule.maxReal() / finalDmg));
            }
        }
    }

    private String resolveByEntity(EntityDamageByEntityEvent event, boolean[] critOut) {
        Entity damager = event.getDamager();

        if (damager instanceof Player attacker) {
            // Zona protegida del atacante tambien cuenta.
            if (plugin.protection().isPvpDenied(attacker)) {
                return null;
            }
            critOut[0] = isMeleeCrit(attacker);
            return weaponKey(attacker.getInventory().getItemInMainHand());
        }

        if (damager instanceof Trident) {
            return "trident";
        }
        if (damager instanceof AbstractArrow arrow) {
            if (arrow.getShooter() instanceof Player shooter && plugin.protection().isPvpDenied(shooter)) {
                return null;
            }
            critOut[0] = arrow.isCritical();
            ItemStack weapon = safeWeapon(arrow);
            return (weapon != null && weapon.getType() == Material.CROSSBOW) ? "crossbow" : "bow";
        }
        if (damager instanceof Firework) {
            return "crossbow"; // cohetes de ballesta (multishot)
        }
        if (damager instanceof EnderCrystal) {
            return "end-crystal";
        }
        if (damager instanceof TNTPrimed) {
            return "explosion";
        }
        if (damager instanceof Projectile) {
            return "projectile"; // bolas de fuego, wind charge, pociones, etc.
        }
        if (damager instanceof LivingEntity) {
            return "unarmed"; // ataque cuerpo a cuerpo de un mob
        }
        return null;
    }

    private String resolveByCause(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case FALL -> "fall";
            case FIRE, FIRE_TICK, LAVA, HOT_FLOOR, CAMPFIRE -> "fire";
            case MAGIC, POISON, WITHER -> "magic";
            case THORNS -> "thorns";
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> "explosion";
            default -> null;
        };
    }

    private String weaponKey(ItemStack weapon) {
        if (weapon == null || weapon.getType() == Material.AIR) {
            return "unarmed";
        }
        String name = weapon.getType().name();
        if (name.endsWith("_SWORD")) {
            return "sword";
        }
        if (name.endsWith("_AXE")) {
            return "axe";
        }
        if (name.endsWith("_SPEAR")) {
            return "spear";
        }
        if (weapon.getType() == Material.TRIDENT) {
            return "trident";
        }
        if (weapon.getType() == Material.MACE) {
            return "mace";
        }
        return "unarmed";
    }

    private boolean isMeleeCrit(Player p) {
        return p.getFallDistance() > 0.0f
                && !p.isOnGround()
                && !p.isSprinting()
                && !p.isInWater()
                && !p.isClimbing()
                && p.getVehicle() == null
                && !p.hasPotionEffect(PotionEffectType.BLINDNESS);
    }

    private ItemStack safeWeapon(AbstractArrow arrow) {
        try {
            return arrow.getWeapon();
        } catch (Throwable ignored) {
            return null; // por si la version no expone getWeapon()
        }
    }
}
