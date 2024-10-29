package info.preva1l.rockworkschat.managers;

import info.preva1l.rockworkschat.config.Config;
import info.preva1l.rockworkschat.models.Channel;
import info.preva1l.rockworkschat.models.Message;
import info.preva1l.rockworkschat.models.Tuple;
import info.preva1l.rockworkschat.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Preva1l
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelManager implements Listener {
    private static ChannelManager instance;

    private final Map<UUID, String> currentChannel = new ConcurrentHashMap<>();

    public static ChannelManager getInstance() {
        if (instance == null) {
            instance = new ChannelManager();
        }
        return instance;
    }

    public void useChannel(UUID player, String channel) {
        currentChannel.put(player, channel);
    }

    public boolean isUsingChannel(UUID player, String channel) {
        return getChannel(player).id().equalsIgnoreCase(channel);
    }

    public Channel getChannel(String id) {
        return Config.getInstance().getChannels().stream().filter(channel -> channel.id().equals(id)).findFirst().orElse(Channel.getDefault());
    }

    public Channel getChannel(UUID player) {
        String channelName = currentChannel.get(player) == null ? "global" : currentChannel.get(player);
        return getChannel(channelName);
    }

    public void dispatchMessageOnChannel(Player player, String channelId, String message) {
        Channel channel = getChannel(channelId);
        // First we send the message to the player, so it looks like there's no delay to the player
        player.sendMessage(Text.modernMessage(channel.format(player, message)));

        // Then we send the message to the rest of the servers, we don't send it to the local server first, so we don't have weird ordering.
        RedisManager.getInstance().send(new Message(Tuple.of(player.getUniqueId(), player.getName()), channel.format(player, message), channel.id()));
    }

    public void dispatchMessage(Player player, String message) {
        Channel channel = getChannel(player.getUniqueId());
        // First we send the message to the player, so it looks like there's no delay to the player
        player.sendMessage(Text.modernMessage(channel.format(player, message)));

        // Then we send the message to the rest of the servers, we don't send it to the local server first, so we don't have weird ordering.
        RedisManager.getInstance().send(new Message(Tuple.of(player.getUniqueId(), player.getName()), channel.format(player, message), channel.id()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatMessage(AsyncChatEvent event) {
        event.setCancelled(true);
        dispatchMessage(event.getPlayer(), MiniMessage.miniMessage().serialize(event.message()));
    }
}
