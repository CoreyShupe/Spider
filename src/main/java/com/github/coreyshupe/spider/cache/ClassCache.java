package com.github.coreyshupe.spider.cache;

import com.github.coreyshupe.spider.wrapper.ClassWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClassCache {
    @NotNull private final Map<String, ClassWrapper> classCache;

    public ClassCache() {
        classCache = new HashMap<>();
    }

    public void addClassEntry(@NotNull ClassWrapper wrapper) {

    }

    public @NotNull Optional<ClassWrapper> loadClassEntry(@NotNull String identifier) {
        return Optional.ofNullable(classCache.getOrDefault(identifier, null));
    }
}
