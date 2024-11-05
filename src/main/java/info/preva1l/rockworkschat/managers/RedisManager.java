package info.preva1l.rockworkschat.managers;

import com.google.gson.Gson;
import info.preva1l.rockworkschat.RockWorksChat;
import info.preva1l.rockworkschat.config.Config;
import info.preva1l.rockworkschat.models.Message;
import info.preva1l.rockworkschat.util.Logger;
import info.preva1l.rockworkschat.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.Pool;

/**
 * @author Preva1l
 */
public final class RedisManager {
    private static RedisManager instance;

    private static String CHANNEL = "NONE";

    private final Gson gson;
    private final Subscriber subscriber;

    private RedisManager() {
        this.gson = new Gson();
        this.subscriber = new Subscriber();
    }

    public static RedisManager getInstance() {
        if (instance == null) {
            instance = new RedisManager();
        }
        return instance;
    }

    @Blocking
    public void connect() throws IllegalStateException {
        final Pool<Jedis> jedisPool = getJedisPool();
        try {
            jedisPool.getResource().ping();
        } catch (JedisException e) {
            throw new IllegalStateException("Failed to establish connection with Redis. "
                    + "Please check the supplied credentials in the config file", e);
        }

        subscriber.enable(jedisPool);
        Thread thread = new Thread(subscriber::subscribe, "rwc:redis_subscriber");
        thread.setDaemon(true);
        thread.start();
    }


    public void send(@NotNull Message message) {
        Bukkit.getScheduler().runTaskAsynchronously(RockWorksChat.i(), () -> subscriber.send(message));
    }

    @Blocking
    public void destroy() {
        subscriber.disable();
    }

    @NotNull
    private static Pool<Jedis> getJedisPool() {
        Config.Broker conf = Config.getInstance().getBroker();
        final String password = conf.getPassword();
        final String host = conf.getHost();
        final int port = conf.getPort();
        CHANNEL = conf.getChannel();

        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(20);
        config.setMaxTotal(50);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        return password.isEmpty()
                ? new JedisPool(config, host, port, 0, false)
                : new JedisPool(config, host, port, 0, password, false);
    }

    private class Subscriber extends JedisPubSub {
        private static final int RECONNECTION_TIME = 8000;

        private Pool<Jedis> jedisPool;
        private boolean enabled;
        private boolean reconnected;

        private void enable(@NotNull Pool<Jedis> jedisPool) {
            this.jedisPool = jedisPool;
            this.enabled = true;
        }

        @Blocking
        private void disable() {
            this.enabled = false;
            if (jedisPool != null && !jedisPool.isClosed()) {
                jedisPool.close();
            }
            this.unsubscribe();
        }

        @Blocking
        public void send(@NotNull Message message) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(CHANNEL, gson.toJson(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Blocking
        private void subscribe() {
            while (enabled && !Thread.interrupted() && jedisPool != null && !jedisPool.isClosed()) {
                try (Jedis jedis = jedisPool.getResource()) {
                    if (reconnected) {
                        Logger.info("Redis connection is alive again");
                    }

                    jedis.subscribe(this, CHANNEL);
                } catch (Throwable t) {
                    onThreadUnlock(t);
                }
            }
        }

        private void onThreadUnlock(@NotNull Throwable t) {
            if (!enabled) {
                return;
            }

            if (reconnected) {
                Logger.warn("Redis Server connection lost. Attempting reconnect in %ss..."
                        .formatted(RECONNECTION_TIME / 1000), t);
            }
            try {
                this.unsubscribe();
            } catch (Throwable ignored) {
            }

            if (!reconnected) {
                reconnected = true;
            } else {
                try {
                    Thread.sleep(RECONNECTION_TIME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public void onMessage(@NotNull String channel, @NotNull String encoded) {
            if (!channel.equals(CHANNEL)) {
                return;
            }
            final Message message;
            try {
                message = gson.fromJson(encoded, Message.class);
            } catch (Exception e) {
                Logger.warn("Failed to decode message from Redis: " + e.getMessage());
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId().equals(message.getSender().getFirst())) return;
                if (!ChannelManager.getInstance().isUsingChannel(player.getUniqueId(), message.getChannel())
                        && !ChannelManager.getInstance().getChannel(message.getChannel()).alwaysView()) return;
                player.sendMessage(Text.modernMessage(message.getMessage()));
            }
        }
    }
}
