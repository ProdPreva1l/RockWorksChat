package info.preva1l.rockworkschat.config;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import info.preva1l.rockworkschat.RockWorksChat;
import info.preva1l.rockworkschat.models.Channel;
import info.preva1l.rockworkschat.util.Logger;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("FieldMayBeFinal")
public final class Config {
    private static Config instance;

    private static final String CONFIG_HEADER = """
            ##########################################
            #             RockWorksChat              #
            #   Stone Works Chat Syncing by Preva1l  #
            ##########################################
            """;

    private List<Channel> channels = List.of(
            Channel.getDefault(),
            new Channel("trade", "&2[TC] &r%player%: %message%", "&2Trade chat enabled!", "default", List.of("trade", "tradechat"), true)
    );

    private Broker broker = new Broker();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Broker {
        private String host = "localhost";
        private int port = 6379;
        private String password = "myAwesomePassword";
        private String channel = "chatsync:message";
    }

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .header(CONFIG_HEADER).build();

    public static void reload() {
        instance = YamlConfigurations.load(new File(RockWorksChat.i().getDataFolder(), "config.yml").toPath(), Config.class, PROPERTIES);
        for (Channel channel : getInstance().getChannels()) {
            channel.registerCommands();
        }
        Logger.info("Configuration automatically reloaded from disk.");
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = YamlConfigurations.update(new File(RockWorksChat.i().getDataFolder(), "config.yml").toPath(), Config.class, PROPERTIES);
            for (Channel channel : getInstance().getChannels()) {
                channel.registerCommands();
            }
            AutoReload.watch(RockWorksChat.i().getDataFolder().toPath(), "config.yml", Config::reload);
        }

        return instance;
    }
}
