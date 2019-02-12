package com.github.lzy.hotfix.log;

/**
 * @author liuzhengyang
 */
public class HotfixLogger {

    private static final HotfixLogger hotfixLogger = new HotfixLogger();

    public static HotfixLogger getLogger() {
        return hotfixLogger;
    }

    public void info(String message, Object... args) {
        System.out.println(message);
    }
}
