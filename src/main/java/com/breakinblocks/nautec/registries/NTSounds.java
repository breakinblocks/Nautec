package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Nautec.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_ENGINE = sound("submarine.engine");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_AMBIENT = sound("submarine.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_DEPLOY = sound("submarine.deploy");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_STOW = sound("submarine.stow");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_SONAR_PING = sound("submarine.sonar_ping");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_BOOST = sound("submarine.boost");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_SHIELD_PULSE = sound("submarine.shield_pulse");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_STEALTH = sound("submarine.stealth");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_LASER_LOOP = sound("submarine.laser_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_TELEPORT_CHARGE = sound("submarine.teleport_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_TELEPORT_WHOOSH = sound("submarine.teleport_whoosh");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_MODULE_INSTALL = sound("submarine.module_install");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_HULL_DAMAGE = sound("submarine.hull_damage");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUBMARINE_LOW_POWER = sound("submarine.low_power");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Nautec.rl(name)));
    }

    private NTSounds() {
    }
}
