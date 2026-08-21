package com.breakinblocks.nautec.registries;

import com.mojang.blaze3d.platform.InputConstants;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class NTKeybinds {
    public static final KeyMapping.Category NAUTEC_CATEGORY = new KeyMapping.Category(Nautec.rl("main"));

    public static final Lazy<KeyMapping> AUGMENT_SCREEN_KEYBIND = keyBind(
            "key.nautec.augment_screen", GLFW.GLFW_KEY_B);


    public static final Lazy<KeyMapping> THROW_TRIDENT_KEYBIND = keyBind(
            "key.nautec.throw_trident", GLFW.GLFW_KEY_Y);

    public static final Lazy<KeyMapping> LEAP_KEYBIND = keyBind(
            "key.nautec.leap", GLFW.GLFW_KEY_LEFT_ALT);

    public static final Lazy<KeyMapping> THROW_POTION_KEYBIND = keyBind(
            "key.nautec.throw_potion", GLFW.GLFW_KEY_G);

    public static final Lazy<KeyMapping> THROW_SPREADING_KEYBIND = keyBind(
            "key.nautec.throw_spreading_trident", GLFW.GLFW_KEY_U);

    public static final Lazy<KeyMapping> ACTIVATE_LASER_KEYBIND = keyBind(
            "key.nautec.activate_laser", GLFW.GLFW_KEY_L);

    public static final Lazy<KeyMapping> SUBMARINE_DESCEND_KEYBIND = keyBind(
            "key.nautec.submarine_descend", GLFW.GLFW_KEY_C);

    public static final Lazy<KeyMapping> SUBMARINE_ABILITY_KEYBIND = keyBind(
            "key.nautec.submarine_ability", GLFW.GLFW_KEY_F);

    public static final Lazy<KeyMapping> WAVE_JET_LIGHT_KEYBIND = Lazy.of(() -> new KeyMapping(
            "key.nautec.wave_jet_light",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, NAUTEC_CATEGORY));

    public static final Lazy<KeyMapping> SUBMARINE_HUD_KEYBIND = Lazy.of(() -> new KeyMapping(
            "key.nautec.submarine_hud",
            KeyConflictContext.IN_GAME,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, NAUTEC_CATEGORY));


    public static Lazy<KeyMapping> keyBind(String name, int key) {
        return Lazy.of(() -> new KeyMapping(name, InputConstants.Type.KEYSYM, key, NAUTEC_CATEGORY));
    }
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(NAUTEC_CATEGORY);
        event.register(AUGMENT_SCREEN_KEYBIND.get());
        event.register(THROW_TRIDENT_KEYBIND.get());
        event.register(LEAP_KEYBIND.get());
        event.register(THROW_POTION_KEYBIND.get());
        event.register(THROW_SPREADING_KEYBIND.get());
        event.register(ACTIVATE_LASER_KEYBIND.get());
        event.register(SUBMARINE_DESCEND_KEYBIND.get());
        event.register(SUBMARINE_ABILITY_KEYBIND.get());
        event.register(WAVE_JET_LIGHT_KEYBIND.get());
        event.register(SUBMARINE_HUD_KEYBIND.get());
    }
}
