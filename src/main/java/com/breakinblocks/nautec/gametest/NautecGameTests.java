package com.breakinblocks.nautec.gametest;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = Nautec.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NautecGameTests {
    public static final String BATCH = Nautec.MODID;

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(NautecGameTests.class);
    }

    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        List<TestFunction> tests = new ArrayList<>();
        tests.add(function("all_guide_references_resolve", Nautec.rl("empty_9x9x9").toString(), 100, 0,
                GuideReferencesGameTest::allGuideReferencesResolve));

        try {
            Class<?> suite = Class.forName("com.breakinblocks.nautec.gametest.suite.NTGameTestRegistration");
            Object registered = suite.getMethod("registerTests").invoke(null);
            if (registered instanceof Collection<?> functions) {
                for (Object function : functions) {
                    tests.add((TestFunction) function);
                }
            }
        } catch (ClassNotFoundException missing) {
            if (Boolean.getBoolean("nautec.requireGameTests")) {
                throw new IllegalStateException("GameTest suites are missing", missing);
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to register nautec gametest suite", t);
        }
        return tests;
    }

    public static String testName(String name) {
        return Nautec.MODID + "." + name.replace('/', '.');
    }

    public static TestFunction function(String name, String structure, int maxTicks, long setupTicks, Consumer<GameTestHelper> body) {
        return new TestFunction(BATCH, testName(name), structure, Rotation.NONE, maxTicks, setupTicks, true, body);
    }
}
