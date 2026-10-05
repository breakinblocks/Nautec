package com.breakinblocks.nautec.utils;

import com.breakinblocks.nautec.NTConfig;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BeamOverclock {
    private float carry;

    public static float speed(long power, long required) {
        if (power <= 0 || power < required) {
            return 0F;
        }
        if (!NTConfig.beamOverclock || required <= 0) {
            return 1F;
        }
        double speed = Math.sqrt(power / (double) required);
        if (NTConfig.beamOverclockMaxSpeed > 0) {
            speed = Math.min(speed, NTConfig.beamOverclockMaxSpeed);
        }
        return (float) Math.max(1.0, speed);
    }

    public int advance(float speed) {
        if (speed <= 0F) {
            return 0;
        }
        carry += speed;
        int steps = (int) carry;
        carry -= steps;
        return steps;
    }

    public void reset() {
        this.carry = 0F;
    }

    public void save(ValueOutput out, String key) {
        out.putFloat(key, carry);
    }

    public void load(ValueInput in, String key) {
        this.carry = in.getFloatOr(key, 0F);
    }
}
