package org.example.service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassLoader {

    public static Class<?> loadClass(String path) throws IOException, ClassNotFoundException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, path);

        if (result != 0) {
            throw new RuntimeException("Compilation failed");
        }

        File classFile = new File(path).getParentFile();
        URLClassLoader classLoader = URLClassLoader.newInstance(
                new URL[]{classFile.toURI().toURL()}
        );

        return Class.forName(extractClassName(path), true, classLoader);

    }

    public static String extractClassName(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        String packageName = extractPackage(content);
        return (!packageName.isEmpty() ? (packageName + ".") : "") + extractSimpleClassName(content);
    }

    private static String extractPackage(String content) {
        Pattern pattern = Pattern.compile("package\\s+([\\w.]+)\\s*;");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String extractSimpleClassName(String content) {
        Pattern pattern = Pattern.compile("(?:public\\s+)?(?:abstract\\s+)?(?:final\\s+)?class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("No class found in file");
    }
}


