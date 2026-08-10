package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.client.renderer.items.SubmarineItemRenderer;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class SubmarineItem extends Item implements IPowerItem, GeoItem {
    private static final double PLACE_REACH = 5.0D;

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
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        Vec3 viewVector = player.getViewVector(1.0F);
        List<Entity> blocking = level.getEntities(player,
                player.getBoundingBox().expandTowards(viewVector.scale(PLACE_REACH)).inflate(1.0D), EntitySelector.CAN_BE_PICKED);
        Vec3 eyes = player.getEyePosition();
        for (Entity entity : blocking) {
            AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
            if (box.contains(eyes)) {
                return InteractionResult.PASS;
            }
        }

        SubmarineEntity submarine = NTEntities.SUBMARINE.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (submarine == null) {
            return InteractionResult.FAIL;
        }

        Vec3 location = hitResult.getLocation();
        submarine.snapTo(location.x, location.y, location.z, player.getYRot(), 0F);
        submarine.applyStack(stack);
        if (level instanceof ServerLevel serverLevel) {
            EntityType.<SubmarineEntity>createDefaultStackConfig(serverLevel, stack, player).accept(submarine);
        }

        if (!level.noCollision(submarine, submarine.getBoundingBox())) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            level.addFreshEntity(submarine);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, location);
            stack.consume(1, player);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
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
        Tooltips.trans(tooltipComponents, "nautec.submarine.controls", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents, "nautec.submarine.aim", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents, "nautec.submarine.oxygen", ChatFormatting.GRAY);
    }
}
