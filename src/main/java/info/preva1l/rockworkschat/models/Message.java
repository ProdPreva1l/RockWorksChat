package info.preva1l.rockworkschat.models;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * @author Preva1l
 */
@Getter
@AllArgsConstructor
public final class Message {
    @Expose private final Tuple<UUID, String> sender;
    @Expose private final String message;
    @Expose private final String channel;
}
