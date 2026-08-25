package me.sirius.sdamage.module.damage;

import org.bukkit.configuration.ConfigurationSection;

public record DamageRule(boolean enabled, double multiplier, double flat,
                         double min, double max, double maxReal) {

    private static final DamageRule DISABLED = new DamageRule(false, 1.0, 0.0, 0.0, -1.0, -1.0);

    public static DamageRule disabled() {
        return DISABLED;
    }

    public static DamageRule from(ConfigurationSection parent, String key) {
        ConfigurationSection s = parent == null ? null : parent.getConfigurationSection(key);
        if (s == null) {
            return DISABLED;
        }
        return new DamageRule(
                s.getBoolean("enabled", true),
                s.getDouble("multiplier", 1.0),
                s.getDouble("flat", 0.0),
                s.getDouble("min", 0.0),
                s.getDouble("max", -1.0),
                s.getDouble("max-real", -1.0)
        );
    }

    public double apply(double base, double extraMult) {
        if (!enabled) {
            return base;
        }
        double value = (base * multiplier * extraMult) + flat;
        if (min >= 0 && value < min) {
            value = min;
        }
        if (max >= 0 && value > max) {
            value = max;
        }
        return Math.max(0.0, value);
    }

    public boolean hasMaxReal() {
        return enabled && maxReal >= 0;
    }
}
