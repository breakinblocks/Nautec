package com.breakinblocks.nautec.capabilities.power;

import org.jetbrains.annotations.Range;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public record EntityPowerStorage(IntSupplier stored, IntConsumer setStored, int capacity, int maxInput,
                                 int maxOutput) implements IPowerStorage {
    @Override
    public int getPowerStored() {
        return this.stored.getAsInt();
    }

    @Override
    public int getPowerCapacity() {
        return this.capacity;
    }

    @Override
    public @Range(from = 0, to = 1) float getPurity() {
        return 1F;
    }

    @Override
    public void setPowerStored(int powerStored) {
        this.setStored.accept(powerStored);
    }

    @Override
    public void setPowerCapacity(int powerCapacity) {
    }

    @Override
    public void setPurity(float purity) {
    }

    @Override
    public int getMaxInput() {
        return this.maxInput;
    }

    @Override
    public int getMaxOutput() {
        return this.maxOutput;
    }
}
