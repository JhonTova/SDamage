package me.sirius.sdamage.command;

import me.sirius.sdamage.SPvP;
import me.sirius.sdamage.module.Module;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SPvPCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB = List.of("reload", "info", "modules");

    private final SPvP plugin;

    public SPvPCommand(SPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("spvp.admin")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadAll();
                plugin.messages().send(sender, "reloaded");
            }
            case "info" -> {
                plugin.messages().sendRaw(sender, "<gray>SPvP <white>v"
                        + plugin.getPluginMeta().getVersion());
                plugin.messages().sendRaw(sender, "<gray>Modulos activos: <white>"
                        + plugin.modules().enabledCount());
                plugin.messages().sendRaw(sender, "<gray>Mundos: <white>"
                        + (plugin.worlds().isAllWorlds() ? "todos"
                            : plugin.worlds().getEnabledWorlds().size()));
                plugin.messages().sendRaw(sender, "<gray>WorldGuard: <white>"
                        + (plugin.protection().isWorldGuardPresent() ? "si" : "no"));
            }
            case "modules" -> {
                plugin.messages().sendRaw(sender, "<gray>Modulos:");
                for (Module m : plugin.modules().getModules()) {
                    String estado;
                    NamedTextColor color;
                    if (m.isActive()) {
                        estado = "activo";
                        color = NamedTextColor.GREEN;
                    } else if (m.isConfigEnabled()) {
                        estado = "ERROR al activar (mira la consola)";
                        color = NamedTextColor.RED;
                    } else {
                        estado = "apagado (enabled: false)";
                        color = NamedTextColor.GRAY;
                    }
                    sender.sendMessage(Component.text(" - " + m.id() + ": ")
                            .color(NamedTextColor.GRAY)
                            .append(Component.text(estado, color)));
                }
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        plugin.messages().sendRaw(sender, "<gray>Comandos:");
        plugin.messages().sendRaw(sender, "<yellow>/spvp reload <gray>- recargar config y modulos");
        plugin.messages().sendRaw(sender, "<yellow>/spvp info <gray>- estado del plugin");
        plugin.messages().sendRaw(sender, "<yellow>/spvp modules <gray>- lista de modulos");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("spvp.admin")) {
            String p = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (String s : SUB) {
                if (s.startsWith(p)) {
                    out.add(s);
                }
            }
            return out;
        }
        return List.of();
    }
}
