package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.gametest.NautecGameTests;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NTTestRegistrar {
    private static final ResourceLocation ARENA = Nautec.rl("empty_9x9x9");

    private final List<TestFunction> functions = new ArrayList<>();

    public int registeredCount() {
        return functions.size();
    }

    public List<TestFunction> functions() {
        return functions;
    }

    public void add(String name, int maxTicks, Consumer<NTGameTestHelper> body) {
        add(name, maxTicks, 0, body);
    }

    public void add(String name, int maxTicks, int setupTicks, Consumer<NTGameTestHelper> body) {
        add(name, ARENA, maxTicks, setupTicks, body);
    }

    public void add(String name, ResourceLocation structure, int maxTicks, int setupTicks, Consumer<NTGameTestHelper> body) {
        try {
            functions.add(NautecGameTests.function(name, structure.toString(), maxTicks, setupTicks,
                    helper -> body.accept(NTGameTestHelper.of(helper))));
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to register gametest " + name, t);
        }
    }
}
