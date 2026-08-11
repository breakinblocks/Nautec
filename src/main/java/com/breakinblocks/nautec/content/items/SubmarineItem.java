package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.client.renderer.items.SubmarineItemRenderer;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleItem;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.utils.ItemUtils;
import com.breakinblocks.nautec.utils.Tooltips;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SubmarineItem extends Item implements IPowerItem, GeoItem {
    private static final double PLACE_REACH = 6.0D;
    private static final double MIN_LAUNCH_DISTANCE = 2.5D;
    private static final double CLEARANCE = 1.8D;

    private final AnimatableInstanceCache animatableCache = new SingletonAnimatableInstanceCache(this);

    public SubmarineItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(NTDataComponents.POWER, ComponentPowerStorage.withCapacity(NTConfig.submarinePowerCapacity)));
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
            private SubmarineItemRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SubmarineItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (SubmarineAnvilRepair.isBreached(stack)) {
            player.sendOverlayMessage(Component.translatable("nautec.submarine.breached").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        SubmarineEntity submarine = NTEntities.SUBMARINE.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (submarine == null) {
            return InteractionResult.FAIL;
        }

        Vec3 launch = findLaunchPoint(level, player, submarine);
        if (launch == null) {
            player.sendOverlayMessage(Component.translatable("nautec.submarine.needs_water").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        submarine.snapTo(launch.x, launch.y, launch.z, player.getYRot(), 0F);
        submarine.applyStack(stack);
        if (level instanceof ServerLevel serverLevel) {
            EntityType.<SubmarineEntity>createDefaultStackConfig(serverLevel, stack, player).accept(submarine);
        }

        if (!level.isClientSide()) {
            level.addFreshEntity(submarine);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, launch);
            stack.consume(1, player);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private static @Nullable Vec3 findLaunchPoint(Level level, Player player, SubmarineEntity submarine) {
        Vec3 eyes = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0F);
        double height = submarine.getBbHeight();

        BlockHitResult solid = level.clip(new ClipContext(eyes, eyes.add(view.scale(PLACE_REACH)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double blocked = solid.getType() == HitResult.Type.BLOCK
                ? eyes.distanceTo(solid.getLocation()) - CLEARANCE
                : PLACE_REACH;

        Vec3 aim;
        if (player.isEyeInFluid(FluidTags.WATER)) {
            double distance = Mth.clamp(blocked, MIN_LAUNCH_DISTANCE, PLACE_REACH);
            aim = eyes.add(view.scale(distance)).subtract(0D, height / 2D, 0D);
        } else {
            BlockHitResult surface = level.clip(new ClipContext(eyes, eyes.add(view.scale(PLACE_REACH)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
            if (surface.getType() != HitResult.Type.BLOCK) {
                return null;
            }
            aim = surface.getLocation();
        }

        Vec3 back = new Vec3(view.x, 0D, view.z).normalize().scale(-1D);
        for (Vec3 offset : List.of(Vec3.ZERO, new Vec3(0D, 0.5D, 0D), new Vec3(0D, 1D, 0D), back, back.add(0D, 0.5D, 0D))) {
            Vec3 candidate = aim.add(offset);
            submarine.snapTo(candidate.x, candidate.y, candidate.z, player.getYRot(), 0F);
            if (fitsInWater(level, submarine)) {
                return candidate;
            }
        }

        return null;
    }

    private static boolean fitsInWater(Level level, SubmarineEntity submarine) {
        AABB hull = submarine.getBoundingBox();
        BlockPos centre = BlockPos.containing(hull.getCenter());
        BlockPos floor = BlockPos.containing(hull.getCenter().subtract(0D, hull.getYsize() / 2D - 0.5D, 0D));

        boolean wet = level.getFluidState(centre).is(FluidTags.WATER) || level.getFluidState(floor).is(FluidTags.WATER);
        return wet && level.noCollision(submarine, hull);
    }

    private static void appendModules(ItemStack stack, Consumer<Component> tooltipComponents) {
        List<Component> installed = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .nonEmptyItemCopyStream()
                .map(SubmarineModuleItem::typeOf)
                .filter(Objects::nonNull)
                .map(SubmarineModuleType::displayName)
                .map(name -> (Component) name)
                .toList();

        if (installed.isEmpty()) {
            return;
        }

        Tooltips.tt(tooltipComponents, Component.translatable("nautec.submarine.modules.installed",
                ComponentUtils.formatList(installed, Component.literal(", "))), ChatFormatting.DARK_AQUA);
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
    public int getMaxInput() {
        return ItemUtils.ITEM_POWER_INPUT;
    }

    @Override
    public int getMaxOutput() {
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        IPowerStorage powerStorage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (powerStorage != null) {
            Tooltips.transInsert(tooltipComponents, "nautec.armor.power",
                    " " + powerStorage.getPowerStored() + "/" + powerStorage.getPowerCapacity(), ChatFormatting.DARK_AQUA);
        }

        if (SubmarineAnvilRepair.isBreached(stack)) {
            Tooltips.trans(tooltipComponents, "nautec.submarine.breached", ChatFormatting.RED);
        } else {
            float max = SubmarineAnvilRepair.maxHealth();
            float health = SubmarineAnvilRepair.healthOf(stack);
            ChatFormatting color = health < max * 0.35F ? ChatFormatting.GOLD : ChatFormatting.DARK_AQUA;
            Tooltips.transInsert(tooltipComponents, "nautec.submarine.hull",
                    " " + Math.round(health / max * 100F) + "%", color);
        }

        appendModules(stack, tooltipComponents);

        Tooltips.trans(tooltipComponents, "nautec.submarine.controls", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents, "nautec.submarine.aim", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents, "nautec.submarine.oxygen", ChatFormatting.GRAY);
    }
}
