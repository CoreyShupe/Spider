package com.github.coreyshupe.spider.reconstructor;

import com.github.coreyshupe.spider.wrapper.ClassWrapper;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClassReconstructor {
    @NotNull private final JavaClass javaClass;

    public ClassReconstructor(@NotNull File file) throws IOException {
        this(fileToJavaClass(file));
    }

    public ClassReconstructor(@NotNull JavaClass javaClass) {
        this.javaClass = javaClass;
    }

    public String reconstructClass() {
        return ClassWrapper.fromClass(javaClass).buildString(0);
    }

    public void dumpResult(@NotNull OutputStream stream) throws IOException {
        stream.write(reconstructClass().getBytes());
    }

    public String getPackageData() {
        return javaClass.getPackageName().replace(".", File.separator);
    }

    private static @NotNull JavaClass fileToJavaClass(@NotNull File file) throws IOException {
        assert file.isFile();
        return new ClassParser(file.getAbsolutePath()).parse();
    }

    private static @NotNull JavaClass jarEntryToJavaClass(@NotNull InputStream inputStream, @NotNull String entry) throws IOException {
        return new ClassParser(inputStream, entry).parse();
    }
}
