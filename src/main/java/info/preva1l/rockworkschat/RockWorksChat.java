package info.preva1l.rockworkschat;

import info.preva1l.rockworkschat.managers.ChannelManager;
import info.preva1l.rockworkschat.managers.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Preva1l
 */
public final class RockWorksChat extends JavaPlugin {
    private static RockWorksChat instance;

    @Override
    public void onEnable() {
        instance = this;
        RedisManager.getInstance().connect();
        Bukkit.getPluginManager().registerEvents(ChannelManager.getInstance(), this);
    }

    @Override
    public void onDisable() {
        RedisManager.getInstance().destroy();
    }

    public static RockWorksChat i() {
        return instance;
    }
}
