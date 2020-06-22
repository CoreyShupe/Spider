package com.github.coreyshupe.spider.wrapper;

import com.github.coreyshupe.spider.AccessModifier;
import com.github.coreyshupe.spider.StringBuildable;
import com.github.coreyshupe.spider.TypeModifierEntries;
import com.github.coreyshupe.spider.TypeUtil;
import lombok.Builder;
import lombok.Getter;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.generic.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@Getter
public class MethodWrapper implements StringBuildable {
    @NotNull private final String className;
    @NotNull private final String qualifiedName;
    @NotNull private final List<ParameterWrapper> parameters;
    @NotNull private final Set<String> exceptions;
    @NotNull private final String returnType;
    @NotNull private final TypeModifierEntries modifierEntries;
    @NotNull private final AccessModifier modifier;
    @NotNull private final Set<AnnotationWrapper> annotations;
    private final boolean isStatic;

    @Override
    public @NotNull String buildString(int indent) {
        String indentation = buildIndentation(indent);
        StringBuilder methodBuilder = new StringBuilder();

        annotations.stream().map(AnnotationWrapper::toString).forEach(str ->
                methodBuilder.append(indentation).append(str).append(NEW_LINE)
        );

        methodBuilder.append(indentation);

        String modifierString = modifier.toString();
        if (modifierString.length() > 0) {
            methodBuilder.append(modifierString).append(' ');
        }

        if (modifierEntries.getEntries().size() > 0) {
            methodBuilder.append(modifierEntries.toString())
                    .append(' ');
        }

        if (qualifiedName.equalsIgnoreCase("<init>")) {
            methodBuilder.append(className);
        } else {
            methodBuilder.append(returnType).append(' ').append(qualifiedName);
        }

        methodBuilder.append('(');
        methodBuilder.append(parameters.stream().map(ParameterWrapper::toString).collect(Collectors.joining(", ")));
        methodBuilder.append(')');
        if (exceptions.size() > 0) {
            methodBuilder.append(" throws ").append(String.join(", ", exceptions));
        }

        if (modifierEntries.isNative()) {
            methodBuilder.append(';');
        } else {
            methodBuilder.append(" {").append(NEW_LINE);
            methodBuilder.append(indentation).append('\t').append("throw new java.lang.UnsupportedOperationException();").append(NEW_LINE);
            methodBuilder.append(indentation).append('}');
        }
        return methodBuilder.toString();
    }

    public static @NotNull MethodWrapper fromMethod(@NotNull Method method, @NotNull String className) {
        List<ParameterWrapper> parameterTypes = new ArrayList<>();
        Type[] argumentTypes = method.getArgumentTypes();
        ParameterAnnotationEntry[] parameterAnnotationEntries = method.getParameterAnnotationEntries();
        for (int i = 0; i < method.getArgumentTypes().length; i++) {
            Type type = argumentTypes[i];
            ParameterAnnotationEntry annotationEntry = parameterAnnotationEntries.length <= i ? null : parameterAnnotationEntries[i];
            parameterTypes.add(ParameterWrapper.fromParameterInfo(type, annotationEntry));
        }
        return MethodWrapper.builder()
                .className(className)
                .qualifiedName(method.getName())
                .parameters(parameterTypes)
                .exceptions(Stream.of(Optional.ofNullable(method.getExceptionTable())
                        .map(ExceptionTable::getExceptionNames)
                        .orElse(new String[0])
                ).collect(Collectors.toSet()))
                .returnType(method.getReturnType().normalizeForStackOrLocal().toString())
                .modifierEntries(TypeModifierEntries.fromFieldOrMethod(method))
                .modifier(AccessModifier.fromAccessible(method))
                .annotations(Stream.of(method.getAnnotationEntries()).map(AnnotationWrapper::fromEntry).collect(Collectors.toSet()))
                .isStatic(method.isStatic())
                .build();
    }

    public static @NotNull MethodWrapper fromMethod(@NotNull Method method, @NotNull String className, @NotNull ImportVisitor importVisitor) {
        List<ParameterWrapper> parameterTypes = new ArrayList<>();
        Type[] argumentTypes = method.getArgumentTypes();
        ParameterAnnotationEntry[] parameterAnnotationEntries = method.getParameterAnnotationEntries();
        for (int i = 0; i < method.getArgumentTypes().length; i++) {
            Type type = argumentTypes[i];
            ParameterAnnotationEntry annotationEntry = parameterAnnotationEntries.length <= i ? null : parameterAnnotationEntries[i];
            parameterTypes.add(ParameterWrapper.fromParameterInfo(type, annotationEntry, importVisitor));
        }

        String[] parts = TypeUtil.reduceStringToImportParts(method.getReturnType().normalizeForStackOrLocal().toString());

        importVisitor.addNecessaryImport(parts[0]);

        return MethodWrapper.builder()
                .className(className)
                .qualifiedName(method.getName())
                .parameters(parameterTypes)
                .exceptions(Stream.of(Optional.ofNullable(method.getExceptionTable())
                        .map(ExceptionTable::getExceptionNames)
                        .orElse(new String[0])
                ).collect(Collectors.toSet()))
                .returnType(parts[1])
                .modifierEntries(TypeModifierEntries.fromFieldOrMethod(method))
                .modifier(AccessModifier.fromAccessible(method))
                .annotations(Stream.of(method.getAnnotationEntries()).map(entry -> AnnotationWrapper.fromEntry(entry, importVisitor)).collect(Collectors.toSet()))
                .isStatic(method.isStatic())
                .build();
    }
}
