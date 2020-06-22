package com.github.coreyshupe.spider;

import org.jetbrains.annotations.NotNull;

public final class TypeUtil {
    private TypeUtil() {
    }

    public static @NotNull String[] reduceStringToImportParts(@NotNull String string) {
        String qualifiedPart = string.substring(string.lastIndexOf('.') + 1);
        return new String[]{isPrimitive(string) ? "" : string, qualifiedPart};
    }

    public static boolean isPrimitive(@NotNull String string) {
        switch (string) {
            case "boolean":
            case "void":
            case "int":
            case "long":
            case "double":
            case "float":
            case "char":
                return true;
            default:
                return false;
        }
    }
}
