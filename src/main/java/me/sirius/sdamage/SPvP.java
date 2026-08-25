package me.sirius.sdamage;

import me.sirius.sdamage.command.SPvPCommand;
import me.sirius.sdamage.config.ConfigManager;
import me.sirius.sdamage.module.ModuleManager;
import me.sirius.sdamage.module.ability.AbilityModule;
import me.sirius.sdamage.module.cooldown.CooldownModule;
import me.sirius.sdamage.module.damage.DamageModule;
import me.sirius.sdamage.module.food.FoodModule;
import me.sirius.sdamage.module.knockback.KnockbackModule;
import me.sirius.sdamage.module.totem.TotemModule;
import me.sirius.sdamage.protection.ProtectionChecker;
import me.sirius.sdamage.util.Messages;
import me.sirius.sdamage.world.WorldManager;
import me.sirius.core.SiriusPlugin;

import java.util.Objects;

public final class SPvP extends SiriusPlugin {

    private ConfigManager configManager;
    private WorldManager worldManager;
    private ProtectionChecker protection;
    private Messages messages;
    private ModuleManager modules;

    @Override
    protected void onStart() {
        // Servicios compartidos.
        this.configManager = new ConfigManager(this);
        this.configManager.reload();

        this.messages = new Messages(this);
        this.worldManager = new WorldManager(this);
        this.protection = new ProtectionChecker(this);

        // Registro de modulos. Anadir uno nuevo = una linea aqui.
        this.modules = new ModuleManager(this);
        this.modules.register(new DamageModule(this));
        this.modules.register(new CooldownModule(this));
        this.modules.register(new FoodModule(this));
        this.modules.register(new TotemModule(this));
        this.modules.register(new KnockbackModule(this));
        this.modules.register(new AbilityModule(this));

        reloadAll();

        // Comandos.
        Objects.requireNonNull(getCommand("spvp"), "comando 'spvp' ausente en plugin.yml")
                .setExecutor(new SPvPCommand(this));

        getLogger().info("SPvP habilitado. Modulos activos: " + modules.activeIds());
    }

    @Override
    protected void onStop() {
        if (modules != null) {
            modules.disableAll();
        }
        getLogger().info("SPvP deshabilitado.");
    }

    public void reloadAll() {
        configManager.reload();
        messages.reload();
        worldManager.reload();
        protection.reload();
        modules.reloadAll();
    }

    public ConfigManager config() {
        return configManager;
    }

    public WorldManager worlds() {
        return worldManager;
    }

    public ProtectionChecker protection() {
        return protection;
    }

    public Messages messages() {
        return messages;
    }

    public ModuleManager modules() {
        return modules;
    }
}
