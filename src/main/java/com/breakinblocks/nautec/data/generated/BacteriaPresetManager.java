package com.breakinblocks.nautec.data.generated;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BacteriaPresetManager extends SimpleJsonResourceReloadListener<BacteriaPreset> {
    public static final String DIRECTORY = Nautec.MODID + "/bacteria_presets";
    public static final Identifier LISTENER_ID = Nautec.rl("bacteria_presets");

    private static Map<Identifier, BacteriaPreset> presets = Map.of();

    public BacteriaPresetManager() {
        super(BacteriaPreset.CODEC, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, BacteriaPreset> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        presets = Map.copyOf(map);
        Nautec.LOGGER.debug("Loaded {} bacteria presets", presets.size());
    }

    public static Collection<BacteriaPreset> all() {
        return List.copyOf(presets.values());
    }

    public static Map<Identifier, BacteriaPreset> byId() {
        return presets;
    }
}
