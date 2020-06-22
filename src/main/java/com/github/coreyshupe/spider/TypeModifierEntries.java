package com.github.coreyshupe.spider;

import lombok.Getter;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.Method;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Getter
public class TypeModifierEntries {
    @NotNull private final Set<String> entries;
    private final boolean isNative;

    public TypeModifierEntries(@NotNull Set<String> entries) {
        this.entries = entries;
        this.isNative = this.entries.contains("native");
    }

    @Override
    public String toString() {
        return String.join(" ", entries);
    }

    public static @NotNull TypeModifierEntries fromFieldOrMethod(@NotNull FieldOrMethod fieldOrMethod) {
        Set<String> entries = new HashSet<>();
        if (fieldOrMethod.isAbstract()) {
            entries.add("abstract");
        }
        if (fieldOrMethod.isFinal()) {
            entries.add("final");
        }
        if (fieldOrMethod.isNative()) {
            entries.add("native");
        }
        if (fieldOrMethod.isVolatile()) {
            entries.add("volatile");
        }
        if (fieldOrMethod.isStatic()) {
            entries.add("static");
        }
        if (fieldOrMethod.isTransient()) {
            entries.add("transient");
        }
        if (fieldOrMethod.isStrictfp() && fieldOrMethod instanceof Method) {
            entries.add("strictfp");
        }
        return new TypeModifierEntries(entries);
    }
}
