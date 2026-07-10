package io.github.gabrielbbaldez.stacktale;

import java.util.Arrays;
import java.util.List;

/** Shared comma-separated-value parsing — both framework appenders configure lists this way. */
public final class Csv {

    private Csv() {}

    /** Splits on commas, trims, drops blanks. Null or blank input → empty list. */
    public static List<String> parse(String s) {
        return s == null || s.isBlank() ? List.of()
                : Arrays.stream(s.split(",")).map(String::trim).filter(x -> !x.isEmpty()).toList();
    }
}
