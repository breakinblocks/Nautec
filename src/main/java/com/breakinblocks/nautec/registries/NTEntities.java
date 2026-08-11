package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.ThrownBouncingTrident;
import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.ThrownSpreadingTrident;
import com.breakinblocks.nautec.content.entities.mobs.AbyssalMaw;
import com.breakinblocks.nautec.content.entities.mobs.LanternJelly;
import com.breakinblocks.nautec.content.entities.mobs.SiltSkipper;
import com.breakinblocks.nautec.content.entities.mobs.VentCrawler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class NTEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Nautec.MODID);

    public static final Supplier<EntityType<ThrownBouncingTrident>> THROWN_BOUNCING_TRIDENT = ENTITIES.register("bouncing_trident",
            ()->EntityType.Builder.<ThrownBouncingTrident>of(ThrownBouncingTrident::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).build(ResourceKey.create(Registries.ENTITY_TYPE, Nautec.rl("bouncing_trident"))));
    public static final Supplier<EntityType<ThrownSpreadingTrident>> THROWN_SPREADING_TRIDENT = ENTITIES.register("spreading_trident",
            () -> EntityType.Builder.<ThrownSpreadingTrident>of(ThrownSpreadingTrident::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build(ResourceKey.create(Registries.ENTITY_TYPE, Nautec.rl("spreading_trident"))));

    public static final Supplier<EntityType<NautecFishingHook>> NAUTEC_FISHING_HOOK = ENTITIES.register("nautec_fishing_hook",
            () -> EntityType.Builder.<NautecFishingHook>of(NautecFishingHook::new, MobCategory.MISC)
                    .noSave().noSummon().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(5)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Nautec.rl("nautec_fishing_hook"))));

    public static final Supplier<EntityType<SubmarineEntity>> SUBMARINE = ENTITIES.register("submarine",
            () -> EntityType.Builder.<SubmarineEntity>of(SubmarineEntity::new, MobCategory.MISC)
                    .sized(3.0f, 3.2f)
                    .eyeHeight(0.5f * SubmarineEntity.MODEL_SCALE).clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Nautec.rl("submarine"))));

    public static final Supplier<EntityType<SiltSkipper>> SILT_SKIPPER = mob("silt_skipper", SiltSkipper::new,
            MobCategory.WATER_AMBIENT, builder -> builder.sized(0.5f, 0.35f).eyeHeight(0.2f));
    public static final Supplier<EntityType<LanternJelly>> LANTERN_JELLY = mob("lantern_jelly", LanternJelly::new,
            MobCategory.WATER_CREATURE, builder -> builder.sized(0.7f, 0.9f).eyeHeight(0.6f));
    public static final Supplier<EntityType<VentCrawler>> VENT_CRAWLER = mob("vent_crawler", VentCrawler::new,
            MobCategory.WATER_CREATURE, builder -> builder.sized(0.9f, 0.5f).eyeHeight(0.35f));
    public static final Supplier<EntityType<AbyssalMaw>> ABYSSAL_MAW = mob("abyssal_maw", AbyssalMaw::new,
            MobCategory.MONSTER, builder -> builder.sized(1.4f, 0.9f).eyeHeight(0.65f));

    private static <T extends net.minecraft.world.entity.Mob> Supplier<EntityType<T>> mob(String name,
                                                                                         EntityType.EntityFactory<T> factory,
                                                                                         MobCategory category,
                                                                                         UnaryOperator<EntityType.Builder<T>> builder) {
        return ENTITIES.register(name, () -> builder.apply(EntityType.Builder.of(factory, category))
                .build(ResourceKey.create(Registries.ENTITY_TYPE, Nautec.rl(name))));
    }
}
