package info.preva1l.rockworkschat.models;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Preva1l
 */
@Getter
@AllArgsConstructor
public class Tuple<F,S> {
    @Expose private F first;
    @Expose private S second;

    public static <F,S> Tuple<F,S> of(F first, S second) {
        return new Tuple<>(first, second);
    }
}
