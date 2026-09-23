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

    public static final DeferredHolder<SoundEvent, SoundEvent> GATEWAY_AMBIENT = sound("gateway.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATEWAY_TRAVEL = sound("gateway.travel");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATEWAY_RECODE = sound("gateway.recode");
    public static final DeferredHolder<SoundEvent, SoundEvent> GATEWAY_UNLINKED = sound("gateway.unlinked");

    public static final DeferredHolder<SoundEvent, SoundEvent> RESONANCE_CHARGE = sound("resonance.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESONANCE_CRAFT = sound("resonance.craft");
    public static final DeferredHolder<SoundEvent, SoundEvent> RESONANCE_VENT = sound("resonance.vent");

    public static final DeferredHolder<SoundEvent, SoundEvent> PRESSURE_FORGE_WORK = sound("pressure_forge.work");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRESSURE_FORGE_COMPLETE = sound("pressure_forge.complete");

    public static final DeferredHolder<SoundEvent, SoundEvent> DOCK_CLAMP = sound("dock.clamp");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOCK_RELEASE = sound("dock.release");

    public static final DeferredHolder<SoundEvent, SoundEvent> OPTICS_HUM = sound("optics.hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUEL_CELL_BURN = sound("fuel_cell.burn");

    public static final DeferredHolder<SoundEvent, SoundEvent> WAVE_JET_START = sound("wave_jet.start");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAVE_JET_LOOP = sound("wave_jet.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAVE_JET_STOP = sound("wave_jet.stop");

    public static final DeferredHolder<SoundEvent, SoundEvent> ATLANTEAN_RIFLE_CHARGE = sound("atlantean_rifle.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ATLANTEAN_RIFLE_FIRE = sound("atlantean_rifle.fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> ATLANTEAN_RIFLE_SPARK = sound("atlantean_rifle.spark");

    public static final DeferredHolder<SoundEvent, SoundEvent> SILT_SKIPPER_AMBIENT = sound("silt_skipper.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SILT_SKIPPER_HURT = sound("silt_skipper.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SILT_SKIPPER_DEATH = sound("silt_skipper.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SILT_SKIPPER_FLOP = sound("silt_skipper.flop");

    public static final DeferredHolder<SoundEvent, SoundEvent> LANTERN_JELLY_AMBIENT = sound("lantern_jelly.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LANTERN_JELLY_HURT = sound("lantern_jelly.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LANTERN_JELLY_DEATH = sound("lantern_jelly.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> LANTERN_JELLY_PULSE = sound("lantern_jelly.pulse");

    public static final DeferredHolder<SoundEvent, SoundEvent> VENT_CRAWLER_AMBIENT = sound("vent_crawler.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> VENT_CRAWLER_HURT = sound("vent_crawler.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> VENT_CRAWLER_DEATH = sound("vent_crawler.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> VENT_CRAWLER_STEP = sound("vent_crawler.step");

    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_MAW_AMBIENT = sound("abyssal_maw.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_MAW_HURT = sound("abyssal_maw.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_MAW_DEATH = sound("abyssal_maw.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAL_MAW_ATTACK = sound("abyssal_maw.attack");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Nautec.rl(name)));
    }

    private NTSounds() {
    }
}
