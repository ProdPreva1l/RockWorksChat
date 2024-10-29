package info.preva1l.rockworkschat.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;

/**
 * @author Preva1l
 */
@UtilityClass
public final class CommandMapUtil {
    private static final Field COMMAND_MAP_FIELD;

    static {
        try {
            COMMAND_MAP_FIELD = SimplePluginManager.class.getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static CommandMap getCommandMap() {
        try {
            return (CommandMap) COMMAND_MAP_FIELD.get(Bukkit.getPluginManager());
        } catch (Exception e) {
            throw new RuntimeException("Could not get CommandMap", e);
        }
    }
}