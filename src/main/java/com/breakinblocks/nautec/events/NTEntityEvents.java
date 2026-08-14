package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.mobs.AbyssalMaw;
import com.breakinblocks.nautec.content.entities.mobs.LanternJelly;
import com.breakinblocks.nautec.content.entities.mobs.SiltSkipper;
import com.breakinblocks.nautec.content.entities.mobs.VentCrawler;
import com.breakinblocks.nautec.registries.NTEntities;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = Nautec.MODID)
public final class NTEntityEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(NTEntities.SILT_SKIPPER.get(), SiltSkipper.createAttributes().build());
        event.put(NTEntities.LANTERN_JELLY.get(), LanternJelly.createAttributes().build());
        event.put(NTEntities.VENT_CRAWLER.get(), VentCrawler.createAttributes().build());
        event.put(NTEntities.ABYSSAL_MAW.get(), AbyssalMaw.createAttributes().build());
        event.put(NTEntities.SUBMARINE.get(), SubmarineEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        registerWaterSpawn(event, NTEntities.SILT_SKIPPER.get(),
                (type, level, reason, pos, random) -> level.getFluidState(pos).is(FluidTags.WATER)
                        && level.getFluidState(pos.above()).is(FluidTags.WATER));

        registerWaterSpawn(event, NTEntities.LANTERN_JELLY.get(),
                (type, level, reason, pos, random) -> level.getFluidState(pos).is(FluidTags.WATER)
                        && level.getFluidState(pos.above()).is(FluidTags.WATER));

        registerWaterSpawn(event, NTEntities.VENT_CRAWLER.get(),
                (type, level, reason, pos, random) -> level.getFluidState(pos).is(FluidTags.WATER));

        registerWaterSpawn(event, NTEntities.ABYSSAL_MAW.get(),
                (type, level, reason, pos, random) -> level.getFluidState(pos).is(FluidTags.WATER)
                        && (reason == EntitySpawnReason.SPAWN_ITEM_USE || pos.getY() < 40)
                        && level.getMaxLocalRawBrightness(pos) <= 7);
    }

    private static <T extends Entity> void registerWaterSpawn(RegisterSpawnPlacementsEvent event, EntityType<T> type,
                                                              SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(type, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
