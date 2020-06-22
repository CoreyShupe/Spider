package com.github.coreyshupe.spider.wrapper;

import com.github.coreyshupe.spider.AccessModifier;
import com.github.coreyshupe.spider.StringBuildable;
import com.github.coreyshupe.spider.TypeModifierEntries;
import com.github.coreyshupe.spider.TypeUtil;
import lombok.Builder;
import lombok.Getter;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Utility;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@Getter
public class FieldWrapper implements StringBuildable {
    @NotNull private final String name;
    @NotNull private final String qualifiedType;
    @Nullable private final String qualifiedConstantValue;
    @NotNull private final Set<AnnotationWrapper> annotations;
    @NotNull private final TypeModifierEntries typeModifierEntries;
    @NotNull private final AccessModifier modifier;
    private final boolean isStatic;

    @Override
    public @NotNull String buildString(int indent) {
        String indentation = buildIndentation(indent);
        StringBuilder fieldBuilder = new StringBuilder();

        annotations.stream().map(AnnotationWrapper::toString).forEach(str ->
                fieldBuilder.append(indentation).append(str).append(NEW_LINE)
        );

        fieldBuilder.append(indentation);

        String modifierString = modifier.toString();
        if (modifierString.length() > 0) {
            fieldBuilder.append(modifierString).append(' ');
        }

        if (typeModifierEntries.getEntries().size() > 0) {
            fieldBuilder.append(typeModifierEntries.toString()).append(' ');
        }

        fieldBuilder.append(qualifiedType)
                .append(' ')
                .append(name);

        if (qualifiedConstantValue != null || !TypeUtil.isPrimitive(qualifiedType)) {
            fieldBuilder.append(" = ").append(qualifiedConstantValue);
        }
        
        fieldBuilder.append(';');

        return fieldBuilder.toString();
    }

    public static @NotNull FieldWrapper fromField(@NotNull Field field) {
        return FieldWrapper.builder()
                .name(field.getName())
                .qualifiedType(field.getType().normalizeForStackOrLocal().toString())
                .qualifiedConstantValue(parseConstantValue(field.getConstantValue()))
                .annotations(Stream.of(field.getAnnotationEntries()).map(AnnotationWrapper::fromEntry).collect(Collectors.toSet()))
                .typeModifierEntries(TypeModifierEntries.fromFieldOrMethod(field))
                .modifier(AccessModifier.fromAccessible(field))
                .isStatic(field.isStatic())
                .build();
    }

    public static @NotNull FieldWrapper fromField(@NotNull Field field, @NotNull ImportVisitor importVisitor) {
        String[] parts = TypeUtil.reduceStringToImportParts(field.getType().normalizeForStackOrLocal().toString());

        importVisitor.addNecessaryImport(parts[0]);

        return FieldWrapper.builder()
                .name(field.getName())
                .qualifiedType(parts[1])
                .qualifiedConstantValue(parseConstantValue(field.getConstantValue()))
                .annotations(Stream.of(field.getAnnotationEntries()).map(entry -> AnnotationWrapper.fromEntry(entry, importVisitor)).collect(Collectors.toSet()))
                .typeModifierEntries(TypeModifierEntries.fromFieldOrMethod(field))
                .modifier(AccessModifier.fromAccessible(field))
                .isStatic(field.isStatic())
                .build();
    }

    @Contract("null -> null")
    private static @Nullable String parseConstantValue(@Nullable ConstantValue value) {
        if (value == null) {
            return null;
        }
        Constant c = value.getConstantPool().getConstant(value.getConstantValueIndex());
        String buf;
        int i;
        switch (c.getTag()) {
            case Const.CONSTANT_Long:
                buf = String.valueOf(((ConstantLong) c).getBytes());
                break;
            case Const.CONSTANT_Float:
                buf = String.valueOf(((ConstantFloat) c).getBytes());
                break;
            case Const.CONSTANT_Double:
                buf = String.valueOf(((ConstantDouble) c).getBytes());
                break;
            case Const.CONSTANT_Integer:
                buf = String.valueOf(((ConstantInteger) c).getBytes());
                break;
            case Const.CONSTANT_String:
                i = ((ConstantString) c).getStringIndex();
                c = value.getConstantPool().getConstant(i, Const.CONSTANT_Utf8);
                buf = "\"" + Utility.convertString(((ConstantUtf8) c).getBytes()) + "\"";
                break;
            default:
                buf = null;
                break;
        }
        return buf;
    }
}
