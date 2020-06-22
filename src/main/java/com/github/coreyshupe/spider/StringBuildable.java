package com.github.coreyshupe.spider;

import org.jetbrains.annotations.NotNull;

public interface StringBuildable {
    String NEW_LINE = System.lineSeparator();

    @NotNull String buildString(int indent);

    default @NotNull String buildIndentation(int indent) {
        StringBuilder indentationBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) indentationBuilder.append('\t');
        return indentationBuilder.toString();
    }
}
