package com.github.coreyshupe.spider.wrapper;

import com.github.coreyshupe.spider.AccessModifier;
import com.github.coreyshupe.spider.StringBuildable;
import lombok.Builder;
import lombok.Getter;
import org.apache.bcel.classfile.JavaClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@Getter
public class ClassWrapper implements StringBuildable {
    @NotNull private final String qualifiedPackage;
    @NotNull private final String qualifiedName;
    @NotNull private final String classType;
    @NotNull private final Set<AnnotationWrapper> annotations;
    @NotNull private final String superClass;
    @NotNull private final Set<String> implementedClasses;
    @NotNull private final AccessModifier modifier;
    @NotNull private final Set<FieldWrapper> fields;
    @NotNull private final Set<MethodWrapper> methods;
    @NotNull private final List<String> imports;

    @Override
    public @NotNull String buildString(int indent) {
        String indentation = buildIndentation(indent);

        StringBuilder classBuilder = new StringBuilder();

        if (indent == 0) {
            classBuilder.append("package ").append(qualifiedPackage).append(';').append(NEW_LINE);
            Collections.sort(imports);
            if (imports.size() > 0) {
                classBuilder.append(NEW_LINE);
                imports.stream()
                        .filter(str -> !str.isEmpty())
                        .forEach(importable -> classBuilder.append("import ").append(importable).append(';').append(NEW_LINE));
            }
            classBuilder.append(NEW_LINE);
        }

        annotations.stream().map(AnnotationWrapper::toString).forEach(str ->
                classBuilder.append(indentation).append(str).append(NEW_LINE)
        );

        classBuilder.append(indentation);

        String modifierString = modifier.toString();
        if (modifierString.length() > 0) {
            classBuilder.append(modifierString).append(' ');
        }

        classBuilder.append(classType).append(' ').append(qualifiedName).append(" {").append(NEW_LINE).append(NEW_LINE);

        fields.stream().sorted((o1, o2) -> Boolean.compare(o1.isStatic(), o2.isStatic()))
                .map(wrapper -> wrapper.buildString(1))
                .forEach(string -> classBuilder.append(string).append(NEW_LINE));

        classBuilder.append(NEW_LINE);

        methods.stream().sorted(((Comparator<MethodWrapper>) (o1, o2) -> Boolean.compare(o1.isStatic(), o2.isStatic())).reversed())
                .map(wrapper -> wrapper.buildString(1))
                .forEach(string -> classBuilder.append(string).append(NEW_LINE).append(NEW_LINE));

        classBuilder.append(indentation).append('}');

        return classBuilder.toString();
    }

    public static @NotNull ClassWrapper fromClass(@NotNull JavaClass javaClass) {
        if (javaClass.isEnum()) {
            // todo we have to do something separate here
            throw new UnsupportedOperationException();
        }

        Set<String> imports = new HashSet<>();

        ImportVisitor visitor = imports::add;

        String trueName = javaClass.getClassName().substring(javaClass.getPackageName().length() + 1);

        return ClassWrapper.builder()
                .qualifiedPackage(javaClass.getPackageName())
                .qualifiedName(trueName)
                .classType(getClassType(javaClass))
                .annotations(Stream.of(javaClass.getAnnotationEntries()).map(entry -> AnnotationWrapper.fromEntry(entry, visitor)).collect(Collectors.toSet()))
                .superClass(javaClass.getSuperclassName())
                .implementedClasses(Stream.of(javaClass.getInterfaceNames()).collect(Collectors.toSet()))
                .modifier(AccessModifier.fromAccessible(javaClass))
                .fields(Stream.of(javaClass.getFields()).map(field -> FieldWrapper.fromField(field, visitor)).collect(Collectors.toSet()))
                .methods(Stream.of(javaClass.getMethods()).map(method -> MethodWrapper.fromMethod(method, trueName, visitor)).collect(Collectors.toSet()))
                .imports(new ArrayList<>(imports))
                .build();
    }

    private static @NotNull String getClassType(JavaClass javaClass) {
        if (javaClass.isEnum()) {
            return "enum";
        } else if (javaClass.isInterface()) {
            return "interface";
        } else if (javaClass.isAnnotation()) {
            return "@interface";
        } else {
            return "class";
        }
    }
}
