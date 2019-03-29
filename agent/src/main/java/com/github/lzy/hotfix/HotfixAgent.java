package com.github.lzy.hotfix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.lzy.hotfix.log.HotfixLogger;

/**
 * @author liuzhengyang
 */
public class HotfixAgent {

    private static final HotfixLogger hotfixLogger = HotfixLogger.getLogger();

    // FIXME multi classes of different classloader ?
    private static Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    public static void agentmain(String agentArgs, Instrumentation instrumentation)
            throws IOException, UnmodifiableClassException, ClassNotFoundException {
        if (agentArgs == null) {
            throw new IllegalArgumentException(agentArgs);
        }
        String[] splits = agentArgs.split(",");
        if (splits.length < 2) {
            throw new IllegalArgumentException(agentArgs);
        }
        hotfixLogger.info("Current Class loader " + HotfixAgent.class.getClassLoader());
        String className = splits[0];
        String replaceTargetClassFile = splits[1];
        File file = Paths.get(replaceTargetClassFile).toFile();
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] newClazzByteCode = new byte[inputStream.available()];
            inputStream.read(newClazzByteCode);
            Class<?> clazz = findTargetClass(className, instrumentation);
            if (clazz == null) {
                ClassLoader classLoader = HotfixAgent.class.getClassLoader();
                try {
                    Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class,
                            byte[].class, int.class, int.class);
                    defineClass.setAccessible(true);
                    clazz = (Class<?>) defineClass.invoke(classLoader, className, newClazzByteCode
                            , 0, newClazzByteCode.length);
                    hotfixLogger.info("Class " + className + " define success " + clazz);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            if (clazz == null) {
                hotfixLogger.info("Class " + className + " not found");
            } else {
                instrumentation.redefineClasses(new ClassDefinition(clazz, newClazzByteCode));
                hotfixLogger.info("Redefine done " + clazz);
            }
        }
    }

    //@VisibleForTest
    static Class<?> findTargetClass(String className, Instrumentation instrumentation) {
        return classCache.computeIfAbsent(className, clazzName -> {
            Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
            return Arrays.stream(allLoadedClasses)
                    .parallel()
                    .filter(klass -> clazzName.equals(klass.getName()))
                    .findFirst()
                    .orElse(null);
        });
    }
}
