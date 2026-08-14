package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.content.augments.AbyssalEyesAugment;
import com.breakinblocks.nautec.content.augments.BonusHeartsAugment;
import com.breakinblocks.nautec.content.augments.CreativeFlightAugment;
import com.breakinblocks.nautec.content.augments.DolphinFinAugment;
import com.breakinblocks.nautec.content.augments.DrownedLungAugment;
import com.breakinblocks.nautec.content.augments.EldritchHeartAugment;
import com.breakinblocks.nautec.content.augments.EnderMagnetAugment;
import com.breakinblocks.nautec.content.augments.GuardianEyeAugment;
import com.breakinblocks.nautec.content.augments.LeapAugment;
import com.breakinblocks.nautec.content.augments.MagnetAugment;
import com.breakinblocks.nautec.content.augments.PhotophoreSkinAugment;
import com.breakinblocks.nautec.content.augments.PreventFallDamageAugment;
import com.breakinblocks.nautec.content.augments.StepUpAugment;
import com.breakinblocks.nautec.content.augments.ThrowBouncingTridentAugment;
import com.breakinblocks.nautec.content.augments.ThrowRandomPotionAugments;
import com.breakinblocks.nautec.content.augments.ThrowSpreadingTrident;
import com.breakinblocks.nautec.content.augments.UnderwaterMiningSpeed;
import com.breakinblocks.nautec.content.augments.VentCarapaceAugment;
import com.breakinblocks.nautec.content.augments.WalkingSpeedAugment;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NTAugments {
    public static final DeferredRegister<AugmentType<?>> AUGMENTS = DeferredRegister.create(NTRegistries.AUGMENT_TYPE, Nautec.MODID);

    public static final Supplier<AugmentType<ThrowRandomPotionAugments>> THROW_POTION_AUGMENT = AUGMENTS.register("throw_random_potion",
            () -> AugmentType.of(ThrowRandomPotionAugments::new, NTAugmentSlots.LEFT_ARM.get(), NTAugmentSlots.RIGHT_ARM.get()));
    public static final Supplier<AugmentType<DrownedLungAugment>> DROWNED_LUNG = AUGMENTS.register("drowned_lung",
            () -> AugmentType.of(DrownedLungAugment::new, NTAugmentSlots.LUNG.get()));
    public static final Supplier<AugmentType<DolphinFinAugment>> DOLPHIN_FIN = AUGMENTS.register("dolphin_fin",
            () -> AugmentType.of(DolphinFinAugment::new, NTAugmentSlots.BODY.get()));
    public static final Supplier<AugmentType<ThrowBouncingTridentAugment>> THROWN_BOUNCING_TRIDENT_AUGMENT = AUGMENTS.register("throw_bouncing_trident",
            () -> AugmentType.of(ThrowBouncingTridentAugment::new, NTAugmentSlots.LEFT_ARM.get(), NTAugmentSlots.RIGHT_ARM.get()));
    public static final Supplier<AugmentType<GuardianEyeAugment>> GUARDIAN_EYE = AUGMENTS.register("guardian_eye",
            () -> AugmentType.of(GuardianEyeAugment::new, NTAugmentSlots.EYES.get()));
    public static final Supplier<AugmentType<LeapAugment>> LEAP_AUGMENT = AUGMENTS.register("leap",
            () -> AugmentType.of(LeapAugment::new, NTAugmentSlots.LEFT_LEG.get(), NTAugmentSlots.RIGHT_LEG.get()));
    public static final Supplier<AugmentType<PreventFallDamageAugment>> PREVENT_FALL_DAMAGE_AUGMENT = AUGMENTS.register("prevent_fall_damage",
            () -> AugmentType.of(PreventFallDamageAugment::new, NTAugmentSlots.LEFT_LEG.get(), NTAugmentSlots.RIGHT_LEG.get()));
    public static final Supplier<AugmentType<StepUpAugment>> STEP_UP_AUGMENT = AUGMENTS.register("step_up",
            () -> AugmentType.of(StepUpAugment::new, NTAugmentSlots.LEFT_LEG.get(), NTAugmentSlots.RIGHT_LEG.get()));
    public static final Supplier<AugmentType<UnderwaterMiningSpeed>> UNDERWATER_MINING_SPEED_AUGMENT = AUGMENTS.register("underwater_mining_speed",
            () -> AugmentType.of(UnderwaterMiningSpeed::new, NTAugmentSlots.LEFT_ARM.get(), NTAugmentSlots.RIGHT_ARM.get()));
    public static final Supplier<AugmentType<BonusHeartsAugment>> BONUS_HEART_AUGMENT = AUGMENTS.register("bonus_hearts",
            () -> AugmentType.of(BonusHeartsAugment::new, NTAugmentSlots.HEART.get()));
    public static final Supplier<AugmentType<CreativeFlightAugment>> CREATIVE_FLIGHT_AUGMENT = AUGMENTS.register("creative_flight",
            () -> AugmentType.of(CreativeFlightAugment::new, NTAugmentSlots.BODY.get()));
    public static final Supplier<AugmentType<MagnetAugment>> MAGNET_AUGMENT = AUGMENTS.register("magnet",
            () -> AugmentType.of(MagnetAugment::new, NTAugmentSlots.LEFT_ARM.get(), NTAugmentSlots.RIGHT_ARM.get()));
    public static final Supplier<AugmentType<EldritchHeartAugment>> ELDRITCH_HEART = AUGMENTS.register("eldritch_heart",
            () -> AugmentType.of(EldritchHeartAugment::new, NTAugmentSlots.HEART.get()));
    public static final Supplier<AugmentType<WalkingSpeedAugment>> WALKING_SPEED_AUGMENT = AUGMENTS.register("walking_speed",
            () -> AugmentType.of(WalkingSpeedAugment::new, NTAugmentSlots.LEFT_LEG.get(), NTAugmentSlots.RIGHT_LEG.get()));
    public static final Supplier<AugmentType<ThrowSpreadingTrident>> SPREADING_TRIDENT_AUGMENT = AUGMENTS.register("spreading_trident",
            () -> AugmentType.of(ThrowSpreadingTrident::new, NTAugmentSlots.LEFT_ARM.get(), NTAugmentSlots.RIGHT_ARM.get()));
    public static final Supplier<AugmentType<EnderMagnetAugment>> ENDER_MAGNET_AUGMENT = AUGMENTS.register("ender_magnet",
            () -> AugmentType.of(EnderMagnetAugment::new, NTAugmentSlots.LEFT_ARM.get(), NTAugmentSlots.RIGHT_ARM.get()));

    public static final Supplier<AugmentType<AbyssalEyesAugment>> ABYSSAL_EYES = AUGMENTS.register("abyssal_eyes",
            () -> AugmentType.of(AbyssalEyesAugment::new, NTAugmentSlots.EYES.get()));
    public static final Supplier<AugmentType<PhotophoreSkinAugment>> PHOTOPHORE_SKIN = AUGMENTS.register("photophore_skin",
            () -> AugmentType.of(PhotophoreSkinAugment::new, NTAugmentSlots.BODY.get()));
    public static final Supplier<AugmentType<VentCarapaceAugment>> VENT_CARAPACE = AUGMENTS.register("vent_carapace",
            () -> AugmentType.of(VentCarapaceAugment::new, NTAugmentSlots.HEAD.get(), NTAugmentSlots.BODY.get()));
}
