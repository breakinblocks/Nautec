package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.client.renderer.items.AtlanteanRifleItemRenderer;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTDamageTypes;
import com.breakinblocks.nautec.utils.ItemUtils;
import com.breakinblocks.nautec.utils.Tooltips;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AtlanteanRifleItem extends Item implements IPowerItem, GeoItem {
    public static final int USE_DURATION = 72000;
    public static final int DAMAGE_INTERVAL = 2;
    private static final int BOW_ENCHANTABILITY = 1;
    private static final float INFINITY_DRAIN_MULTIPLIER = 0.5F;
    private static final int HIT_POPS_MIN = 3;
    private static final int HIT_POPS_MAX = 10;
    private static final double HIT_POP_SPEED = 0.02D;
    public static final DataTicket<Float> USE_TICKS = DataTicket.create("nautec:atlantean_rifle_use_ticks", Float.class);

    private final AnimatableInstanceCache animatableCache = new SingletonAnimatableInstanceCache(this);

    public AtlanteanRifleItem(Properties properties) {
        super(properties.stacksTo(1).enchantable(BOW_ENCHANTABILITY)
                .component(NTDataComponents.POWER, ComponentPowerStorage.withCapacity(NTConfig.riflePowerCapacity))
                .component(DataComponents.USE_EFFECTS, new UseEffects(true, UseEffects.DEFAULT.interactVibrations(), 1.0F)));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AtlanteanRifleItemRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new AtlanteanRifleItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public int getMaxInput() {
        return NTConfig.rifleMaxInput;
    }

    @Override
    public int getMaxOutput() {
        return 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ItemUtils.POWER_BAR_COLOR;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ItemUtils.powerForDurabilityBar(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!hasPower(stack, player, drainFor(stack, 0))) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("nautec.atlantean_rifle.no_power").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.PASS;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) {
            return;
        }

        int firing = firingTicks(getUseDuration(stack, entity) - remaining);
        if (firing < 0 || firing % DAMAGE_INTERVAL != 0) {
            return;
        }

        if (!drain(stack, player, drainFor(stack, firing))) {
            player.stopUsingItem();
            return;
        }

        fire(serverLevel, player, damageFor(firing));
    }

    public static @Nullable Entity fire(ServerLevel level, LivingEntity shooter, float damage) {
        AtlanteanRifleBeam.Hit hit = AtlanteanRifleBeam.trace(level, shooter, NTConfig.rifleRange, 1.0F);
        Entity target = hit.entity();
        if (target != null) {
            DamageSource source = level.damageSources().source(NTDamageTypes.PARTICLE_BEAM, shooter);
            if (target.hurtServer(level, source, damage)) {
                popTarget(level, target, damage);
            }
        }
        return target;
    }

    private static void popTarget(ServerLevel level, Entity target, float damage) {
        float ramp = Mth.clamp(Mth.inverseLerp(damage, (float) NTConfig.rifleBaseDamage, (float) NTConfig.rifleMaxDamage), 0F, 1F);
        int count = HIT_POPS_MIN + Math.round(ramp * (HIT_POPS_MAX - HIT_POPS_MIN));
        AABB box = target.getBoundingBox();
        Vec3 centre = box.getCenter();
        level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, centre.x, centre.y, centre.z, count,
                box.getXsize() * 0.35D, box.getYsize() * 0.35D, box.getZsize() * 0.35D, HIT_POP_SPEED);
    }

    public static int firingTicks(int ticksUsing) {
        return ticksUsing - NTConfig.rifleChargeTicks;
    }

    public static float firingTicks(LivingEntity holder, float partialTick) {
        return isUsing(holder) ? holder.getTicksUsingItem(partialTick) - NTConfig.rifleChargeTicks : Float.NEGATIVE_INFINITY;
    }

    public static boolean isUsing(LivingEntity holder) {
        return holder.isUsingItem() && holder.getUseItem().getItem() instanceof AtlanteanRifleItem;
    }

    public static boolean isFiring(LivingEntity holder) {
        return isUsing(holder) && firingTicks(holder.getTicksUsingItem()) >= 0;
    }

    public static float rampProgress(float firingTicks) {
        return Mth.clamp(firingTicks / NTConfig.rifleRampTicks, 0F, 1F);
    }

    public static float damageFor(int firingTicks) {
        return Mth.lerp(rampProgress(firingTicks), (float) NTConfig.rifleBaseDamage, (float) NTConfig.rifleMaxDamage);
    }

    public static int drainFor(int firingTicks) {
        return Math.round(Mth.lerp(rampProgress(firingTicks), NTConfig.rifleBaseDrain, NTConfig.rifleMaxDrain));
    }

    public static int drainFor(ItemStack stack, int firingTicks) {
        int drain = drainFor(firingTicks);
        return hasInfinity(stack) ? Math.round(drain * INFINITY_DRAIN_MULTIPLIER) : drain;
    }

    public static boolean hasInfinity(ItemStack stack) {
        for (Holder<Enchantment> enchantment : stack.getEnchantments().keySet()) {
            if (enchantment.is(Enchantments.INFINITY)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPower(ItemStack stack, Player player, int amount) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        IPowerStorage storage = storage(stack);
        return storage != null && storage.getPowerStored() >= amount;
    }

    private static boolean drain(ItemStack stack, Player player, int amount) {
        if (!hasPower(stack, player, amount)) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            IPowerStorage storage = storage(stack);
            storage.setPowerStored(storage.getPowerStored() - amount);
        }
        return true;
    }

    private static @Nullable IPowerStorage storage(ItemStack stack) {
        return stack.getCapability(NTCapabilities.PowerStorage.ITEM);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        IPowerStorage powerStorage = storage(stack);
        if (powerStorage != null) {
            Tooltips.transInsert(tooltipComponents, "nautec.armor.power",
                    " " + powerStorage.getPowerStored() + "/" + powerStorage.getPowerCapacity(), ChatFormatting.DARK_AQUA);
        }
        Tooltips.trans(tooltipComponents, "nautec.atlantean_rifle.hint", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents, "nautec.atlantean_rifle.ramp", ChatFormatting.GRAY);
        if (hasInfinity(stack)) {
            Tooltips.trans(tooltipComponents, "nautec.atlantean_rifle.infinity", ChatFormatting.DARK_AQUA);
        }
    }
}
