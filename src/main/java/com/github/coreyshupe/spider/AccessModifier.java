package com.github.coreyshupe.spider;

import org.apache.bcel.classfile.AccessFlags;
import org.jetbrains.annotations.NotNull;

public enum AccessModifier {
    PUBLIC("public"),
    PRIVATE("private"),
    PACKAGE(""),
    PROTECTED("protected");

    @NotNull private final String qualifiedName;

    AccessModifier(@NotNull String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public static @NotNull AccessModifier fromAccessible(@NotNull AccessFlags accessFlags) {
        if (accessFlags.isPrivate()) {
            return AccessModifier.PRIVATE;
        } else if (accessFlags.isPublic()) {
            return AccessModifier.PUBLIC;
        } else if (accessFlags.isProtected()) {
            return AccessModifier.PROTECTED;
        } else {
            return AccessModifier.PACKAGE;
        }
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
