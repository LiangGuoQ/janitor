package com.janitor.common.etcd;

import io.etcd.jetcd.ByteSequence;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * ClassName Utils
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 14:19
 */
public class Utils {
    private static final byte[] NO_PREFIX_END = new byte[]{0};
    public static final String ALL_KEY_CHAR = "\u0000";
    public static final ByteSequence ALL_KEY_CHAR_BS;

    public Utils() {
    }

    public static String bsToStr(ByteSequence bs) {
        return Optional.ofNullable(bs).map((b) -> b.toString(StandardCharsets.UTF_8)).orElse("");
    }

    public static ByteSequence strToBs(String str) {
        return ByteSequence.from(str, StandardCharsets.UTF_8);
    }

    public static ByteSequence prefixEndOf(ByteSequence prefix) {
        byte[] endKey = prefix.getBytes().clone();

        for (int i = endKey.length - 1; i >= 0; --i) {
            if (endKey[i] != -1) {
                ++endKey[i];
                return ByteSequence.from(Arrays.copyOf(endKey, i + 1));
            }
        }

        return ByteSequence.from(NO_PREFIX_END);
    }

    static {
        ALL_KEY_CHAR_BS = ByteSequence.from("\u0000", StandardCharsets.UTF_8);
    }
}

