package me.sirius.sdamage.module;

import me.sirius.sdamage.SPvP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {

    private final SPvP plugin;
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager(SPvP plugin) {
        this.plugin = plugin;
    }

    public void register(Module module) {
        modules.add(module);
    }

    public void reloadAll() {
        for (Module m : modules) {
            m.disable();
            if (m.isConfigEnabled()) {
                try {
                    m.enable();
                } catch (Exception ex) {
                    plugin.getLogger().severe("No se pudo activar el modulo '" + m.id()
                            + "': " + ex.getMessage());
                }
            }
        }
    }

    public void disableAll() {
        for (Module m : modules) {
            m.disable();
        }
    }

    public long enabledCount() {
        return modules.stream().filter(Module::isConfigEnabled).count();
    }

    public String activeIds() {
        return modules.stream()
                .filter(Module::isActive)
                .map(Module::id)
                .reduce((a, b) -> a + ", " + b)
                .orElse("ninguno");
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }
}
