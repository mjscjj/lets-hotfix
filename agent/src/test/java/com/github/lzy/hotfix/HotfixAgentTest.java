package com.github.lzy.hotfix;

import static org.junit.Assert.*;

import java.lang.instrument.Instrumentation;

import org.junit.Test;

import net.bytebuddy.agent.ByteBuddyAgent;

public class HotfixAgentTest {

    @Test
    public void findTargetClass() throws Exception {
        DummyService dummyService = new DummyService();
        Instrumentation instrumentation = ByteBuddyAgent.install();
        Class<?> targetClass = HotfixAgent.findTargetClass("com.github.lzy.hotfix.HotfixAgentTest.DummyService", instrumentation);
        assertNotNull(targetClass);
        assertEquals(targetClass, dummyService.getClass());
    }

    static class DummyService {
        public String foo() {
            return "foo";
        }
    }
}