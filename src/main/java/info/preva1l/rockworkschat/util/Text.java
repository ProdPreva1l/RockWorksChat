package info.preva1l.rockworkschat.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Preva1l
 */
@UtilityClass
public final class Text {
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");

    /**
     * Converts legacy colour codes to MiniMessage.
     *
     * @param message message with legacy codes
     * @return string with mini modernMessage formatting (not colorized)
     */
    public String legacyToMiniMessage(String message) {
        message = message.replace("&4", "<dark_red>");
        message = message.replace("&c", "<red>");
        message = message.replace("&6", "<gold>");
        message = message.replace("&e", "<yellow>");
        message = message.replace("&2", "<dark_green>");
        message = message.replace("&a", "<green>");
        message = message.replace("&b", "<aqua>");
        message = message.replace("&3", "<dark_aqua>");
        message = message.replace("&1", "<dark_blue>");
        message = message.replace("&9", "<blue>");
        message = message.replace("&d", "<light_purple>");
        message = message.replace("&5", "<dark_purple>");
        message = message.replace("&f", "<white>");
        message = message.replace("&7", "<gray>");
        message = message.replace("&8", "<dark_gray>");
        message = message.replace("&0", "<black>");
        message = message.replace("&l", "<b>");
        message = message.replace("&k", "<obf>");
        message = message.replace("&m", "<st>");
        message = message.replace("&n", "<u>");
        message = message.replace("&o", "<i>");
        message = message.replace("&r", "<reset>");

        Matcher match = LEGACY_HEX_PATTERN.matcher(message);
        String code = message;
        while (match.find()) {
            code = message.substring(match.start(), match.end());
            code = code.replace("&", "<");
            code = code + ">";
        }
        return message.replaceAll("&#[a-fA-F0-9]{6}", code);
    }

    /**
     * Takes a string formatted in minimessage OR legacy and turns it into an Adventure Component.
     *
     * @param message the modernMessage
     * @return colorized component
     */
    public Component modernMessage(@NotNull String message) {
        return MiniMessage.miniMessage().deserialize(legacyToMiniMessage(message));
    }
}