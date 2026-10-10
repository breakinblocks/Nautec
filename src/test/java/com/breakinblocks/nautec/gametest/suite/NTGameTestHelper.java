package com.breakinblocks.nautec.gametest.suite;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Field;

public class NTGameTestHelper extends GameTestHelper {
    private static final Field TEST_INFO = findTestInfoField();

    public NTGameTestHelper(GameTestInfo testInfo) {
        super(testInfo);
    }

    public static NTGameTestHelper of(GameTestHelper helper) {
        if (helper instanceof NTGameTestHelper nautec) {
            return nautec;
        }
        try {
            return new NTGameTestHelper((GameTestInfo) TEST_INFO.get(helper));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read the GameTestInfo of a helper", e);
        }
    }

    private static Field findTestInfoField() {
        for (Field field : GameTestHelper.class.getDeclaredFields()) {
            if (field.getType() == GameTestInfo.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("GameTestHelper has no GameTestInfo field");
    }

    public <T extends BlockEntity> T getBlockEntity(BlockPos pos, Class<T> type) {
        BlockEntity blockEntity = getLevel().getBlockEntity(absolutePos(pos));
        if (!type.isInstance(blockEntity)) {
            throw assertionException("Expected a " + type.getSimpleName() + " at " + pos + " but found " + blockEntity);
        }
        return type.cast(blockEntity);
    }

    public GameTestAssertException assertionException(String message) {
        return new GameTestAssertException(message);
    }
}
