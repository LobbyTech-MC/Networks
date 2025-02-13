package com.balugaq.netex.api.helpers;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.sefiraat.networks.Networks;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class LanguageHelper {
    private static final Gson GSON = new Gson();
    private static Map<String, String> lang = new HashMap<>();

    public static void loadFromStream(@Nonnull InputStream stream) {
        Preconditions.checkArgument(stream != null, "输入流不能为空");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        Type type = (new TypeToken<Map<String, String>>() {
        }).getType();
        lang = GSON.fromJson(reader, type);
    }

    @Nonnull
    public static String getLangOrDefault(@Nonnull String key, @Nonnull String defaultVal) {
        String lang = getLangOrNull(key);
        return lang != null ? lang : defaultVal;
    }

    @Nonnull
    public static String getLangOrKey(@Nonnull String key) {
        return getLangOrDefault(key, key);
    }

    @Nullable
    public static String getLangOrNull(@Nonnull String key) {
        Preconditions.checkArgument(key != null, "键名不能为空");
        return Networks.getLocalizationService().getMCMessage(key);
    }
}
