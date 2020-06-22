package com.github.coreyshupe.spider.wrapper;

import com.github.coreyshupe.spider.Main;
import com.github.coreyshupe.spider.TypeUtil;
import lombok.Builder;
import lombok.Getter;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassElementValue;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.EnumElementValue;
import org.apache.bcel.classfile.SimpleElementValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Getter
public class AnnotationWrapper {
    @NotNull private final String qualifiedType;
    @NotNull private final Map<String, String> typeValuePairs;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('@').append(qualifiedType);
        if (typeValuePairs.size() > 0) {
            builder.append('(');
            builder.append(typeValuePairs.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + " = " + entry.getValue())
                    .collect(Collectors.joining(", ")));
            builder.append(')');
        }
        return builder.toString();
    }

    public static @NotNull AnnotationWrapper fromEntry(@NotNull AnnotationEntry entry) {
        Map<String, String> typeValuePairs = new HashMap<>();
        for (ElementValuePair elementValuePair : entry.getElementValuePairs()) {
            ElementValue value = elementValuePair.getValue();
            String valueString = value.toShortString();
            if (value instanceof EnumElementValue) {
                String formatted = fixUnformattedTypeString(((EnumElementValue) value).getEnumTypeString());
                valueString = formatted + "." + valueString;
            } else if (value.getElementValueType() == ElementValue.STRING) {
                valueString = '"' + valueString + '"';
            }
            typeValuePairs.put(elementValuePair.getNameString(), valueString);
        }

        String type = fixUnformattedTypeString(entry.getAnnotationType());

        return AnnotationWrapper.builder()
                .qualifiedType(type)
                .typeValuePairs(typeValuePairs)
                .build();
    }

    public static @NotNull AnnotationWrapper fromEntry(@NotNull AnnotationEntry entry, @NotNull ImportVisitor importVisitor) {
        Map<String, String> typeValuePairs = new HashMap<>();
        for (ElementValuePair elementValuePair : entry.getElementValuePairs()) {
            ElementValue value = elementValuePair.getValue();
            String valueString = value.toShortString();
            if (value instanceof EnumElementValue) {
                String formatted = fixUnformattedTypeString(((EnumElementValue) value).getEnumTypeString());
                String[] parts = TypeUtil.reduceStringToImportParts(formatted);
                importVisitor.addNecessaryImport(parts[0]);
                valueString = parts[1] + "." + valueString;
            } else if (value.getElementValueType() == ElementValue.STRING) {
                valueString = '"' + valueString + '"';
            }
            typeValuePairs.put(elementValuePair.getNameString(), valueString);
        }

        String type = fixUnformattedTypeString(entry.getAnnotationType());

        String[] parts = TypeUtil.reduceStringToImportParts(type);

        importVisitor.addNecessaryImport(parts[0]);

        return AnnotationWrapper.builder()
                .qualifiedType(parts[1])
                .typeValuePairs(typeValuePairs)
                .build();
    }

    private static @NotNull String fixUnformattedTypeString(@NotNull String string) {
        return string.substring(1, string.length() - 1).replace('/', '.');
    }
}
