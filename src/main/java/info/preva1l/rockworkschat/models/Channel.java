package info.preva1l.rockworkschat.models;

import info.preva1l.rockworkschat.managers.ChannelManager;
import info.preva1l.rockworkschat.util.CommandMapUtil;
import info.preva1l.rockworkschat.util.Text;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Preva1l
 */
public record Channel(
        @NotNull String id,
        @NotNull String format,
        @NotNull String toggleMessage,
        @NotNull String permission,
        @NotNull List<String> commands,
        boolean alwaysView
) {
    public static Channel getDefault() {
        return new Channel("global", "%vault_prefix% &f%player%: &7%message%", "&fNow using &aGlobal &fchat!", "default", List.of("global", "globalchat"), true);
    }

    public String format(Player player, String message) {
        message = format
                .replace("%message%", message)
                .replace("%player%", player.getName());
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return message;
        }
        return PlaceholderAPI.setPlaceholders(player, message);
    }

    public void registerCommands() {
        for (String command : commands) {
            CommandMapUtil.getCommandMap().register("chat", new RegisterCommand(command));
        }
    }

    public class RegisterCommand extends Command {
        protected RegisterCommand(@NotNull String name) {
            super(name);
            if (permission.isEmpty() || permission.equalsIgnoreCase("default")) return;
            this.setPermission(permission);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            if (!(sender instanceof Player player)) return false;
            if (args.length < 1 || args[0].equalsIgnoreCase("toggle")) {
                ChannelManager.getInstance().useChannel(player.getUniqueId(), id);
                player.sendMessage(Text.modernMessage(toggleMessage));
                return true;
            }

            ChannelManager.getInstance().dispatchMessageOnChannel(player, id, String.join(" ", args));
            return false;
        }
    }
}
