package com.github.coreyshupe.spider.wrapper;

import com.github.coreyshupe.spider.TypeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.generic.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public class ParameterWrapper {
    @NotNull private final String qualifiedType;
    @NotNull private final Set<AnnotationWrapper> annotations;

    @Override
    public String toString() {
        StringBuilder parameterBuilder = new StringBuilder();
        if (annotations.size() > 0) {
            parameterBuilder.append(annotations.stream().map(AnnotationWrapper::toString).collect(Collectors.joining(" ")));
            parameterBuilder.append(" ");
        }
        return parameterBuilder.append(qualifiedType).append(" ").append(normalizeType()).toString();
    }

    private String normalizeType() {
        String next = qualifiedType.substring(qualifiedType.lastIndexOf('.') + 1);
        return Character.toLowerCase(next.charAt(0)) + next.substring(1);
    }

    public static @NotNull ParameterWrapper fromParameterInfo(@NotNull Type type, @Nullable ParameterAnnotationEntry annotations) {
        return new ParameterWrapper(
                type.normalizeForStackOrLocal().toString(),
                Stream.of(Optional.ofNullable(annotations).map(ParameterAnnotationEntry::getAnnotationEntries).orElse(new AnnotationEntry[0]))
                        .map(AnnotationWrapper::fromEntry).collect(Collectors.toSet())
        );
    }

    public static @NotNull ParameterWrapper fromParameterInfo(@NotNull Type type, @Nullable ParameterAnnotationEntry annotations, @NotNull ImportVisitor importVisitor) {
        String[] parts = TypeUtil.reduceStringToImportParts(type.normalizeForStackOrLocal().toString());

        importVisitor.addNecessaryImport(parts[0]);

        return new ParameterWrapper(
                parts[1],
                Stream.of(Optional.ofNullable(annotations).map(ParameterAnnotationEntry::getAnnotationEntries).orElse(new AnnotationEntry[0]))
                        .map(AnnotationWrapper::fromEntry).collect(Collectors.toSet())
        );
    }
}
