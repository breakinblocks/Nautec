package com.breakinblocks.nautec.content.blockentities.multiblock.controller;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.network.OpenAugmentationScreenPayload;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.network.PacketDistributor;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.AugmentationStationExtensionBlockEntity;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.AugmentationRecipeInput;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AugmentationStationBlockEntity extends ContainerBlockEntity implements MultiblockEntity {
    private MultiblockData multiblockData;
    private UUID playerUUID;
    private int playerOpenMenuInterval;

    private final Map<BlockPos, ItemStack> augmentItems;

    private boolean isRunning;
    private int duration;
    private Player player;
    private AugmentationRecipe recipe;
    private AugmentSlot slot;

    private static final Identifier SURGERY_LOCK = Nautec.rl("augmentation_lock");
    private final Map<BlockPos, ItemStack> operationInputs = new HashMap<>();

    public AugmentationStationBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.AUGMENTATION_STATION.get(), blockPos, blockState);
        this.multiblockData = MultiblockData.EMPTY;
        this.augmentItems = new HashMap<>();
        this.player = null;
    }

    public Map<BlockPos, ItemStack> getAugmentItems() {
        return augmentItems;
    }

    public void startAugmentation(Player player, AugmentSlot augmentSlot) {
        if (!(level instanceof ServerLevel) || isRunning || !canOperateOn(player)
                || player.getData(NTDataAttachments.AUGMENTATION_STATION).isPresent()) {
            return;
        }
        Optional<AugmentationRecipe> candidate = getRecipe();
        if (candidate.isEmpty() || !candidate.get().resultAugment().getAugmentSlots().contains(augmentSlot)) {
            return;
        }
        for (BlockPos pos : augmentItems.keySet()) {
            if (!(level.getBlockEntity(pos) instanceof AugmentationStationExtensionBlockEntity extension)
                    || extension.getPower() < NTConfig.augmentationStationPower) {
                return;
            }
        }

        this.operationInputs.clear();
        augmentItems.forEach((pos, stack) -> operationInputs.put(pos, stack.copy()));
        this.player = player;
        this.recipe = candidate.get();
        this.slot = augmentSlot;
        this.duration = 80;
        this.isRunning = true;
        player.setData(NTDataAttachments.AUGMENTATION_STATION, Optional.of(GlobalPos.of(level.dimension(), worldPosition)));
        for (var attribute : List.of(Attributes.MOVEMENT_SPEED, Attributes.JUMP_STRENGTH)) {
            var instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.addOrUpdateTransientModifier(new AttributeModifier(SURGERY_LOCK, -1,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
        for (BlockPos pos : operationInputs.keySet()) {
            ((AugmentationStationExtensionBlockEntity) level.getBlockEntity(pos)).equipAugment();
        }
    }

    private boolean canOperateOn(Player player) {
        return player != null && player.level() == level && player.isAlive() && !player.isRemoved()
                && !player.isSpectator() && isFormed() && extensionsPresent()
                && player.getBoundingBox().intersects(new AABB(worldPosition.above()));
    }

    private boolean extensionsPresent() {
        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            BlockPos pos = worldPosition.relative(direction, 2);
            if (!level.isLoaded(pos)
                    || !(level.getBlockEntity(pos) instanceof AugmentationStationExtensionBlockEntity extension)
                    || !extension.getBlockState().getValue(Multiblock.FORMED)
                    || !worldPosition.equals(extension.getControllerPos())) {
                return false;
            }
        }
        return true;
    }

    public @NotNull Optional<AugmentationRecipe> getRecipe() {
        if (!(level instanceof ServerLevel serverLevel) || !isFormed() || !extensionsPresent()) {
            return Optional.empty();
        }
        List<ItemStack> ingredients = collectInputItems();
        return serverLevel.recipeAccess()
                .getRecipeFor(AugmentationRecipe.Type.INSTANCE, new AugmentationRecipeInput(ingredients, 100), level)
                .map(RecipeHolder::value);
    }

    public boolean isOperatingOn(Player player) {
        return isRunning && this.player == player && canOperateOn(player);
    }

    public void restorePlayerAttributes() {
        if (player != null) {
            releasePlayer(player);
        }
    }

    private static void releasePlayer(Player player) {
        for (var attribute : List.of(Attributes.MOVEMENT_SPEED, Attributes.JUMP_STRENGTH)) {
            var instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(SURGERY_LOCK);
            }
        }
        player.setData(NTDataAttachments.AUGMENTATION_STATION, Optional.empty());
    }

    public void cancelAugmentation() {
        restorePlayerAttributes();
        this.isRunning = false;
        this.duration = 0;
        this.player = null;
        this.recipe = null;
        this.slot = null;
        this.operationInputs.clear();
    }

    public static void cancelFor(Player player) {
        player.getData(NTDataAttachments.AUGMENTATION_STATION).ifPresent(pos -> {
            if (pos.dimension().equals(player.level().dimension()) && player.level().isLoaded(pos.pos())
                    && player.level().getBlockEntity(pos.pos()) instanceof AugmentationStationBlockEntity station
                    && station.player == player) {
                station.cancelAugmentation();
            }
        });
        releasePlayer(player);
    }

    public static void checkPlayer(Player player) {
        player.getData(NTDataAttachments.AUGMENTATION_STATION).ifPresent(pos -> {
            if (!pos.dimension().equals(player.level().dimension()) || !player.level().isLoaded(pos.pos())
                    || !(player.level().getBlockEntity(pos.pos()) instanceof AugmentationStationBlockEntity station)
                    || !station.isOperatingOn(player)) {
                cancelFor(player);
            }
        });
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        if (isRunning) {
            if (!canOperateOn(player)) {
                cancelAugmentation();
                return;
            }
            for (var input : operationInputs.entrySet()) {
                var extension = (AugmentationStationExtensionBlockEntity) level.getBlockEntity(input.getKey());
                if (extension.getPower() < NTConfig.augmentationStationPower
                        || !ItemStack.matches(input.getValue(), extension.getAugmentItem())) {
                    cancelAugmentation();
                    return;
                }
            }
            if (--duration <= 0) {
                Player recipient = player;
                AugmentationRecipe completedRecipe = recipe;
                AugmentSlot completedSlot = slot;
                for (BlockPos pos : operationInputs.keySet()) {
                    var extension = (AugmentationStationExtensionBlockEntity) level.getBlockEntity(pos);
                    extension.getItemStackHandler().extractItem(0, 1, false);
                }
                cancelAugmentation();
                AugmentHelper.createAugment(completedRecipe.resultAugment(), recipient, completedSlot);
            }
            return;
        }
        if (!isFormed()) {
            playerUUID = null;
            return;
        }
        List<Player> occupants = level.getEntitiesOfClass(Player.class, new AABB(worldPosition.above()), this::canOperateOn);
        if (occupants.isEmpty()) {
            playerUUID = null;
            return;
        }
        Player occupant = occupants.getFirst();
        if (!occupant.getUUID().equals(playerUUID)) {
            playerUUID = occupant.getUUID();
            playerOpenMenuInterval = 10;
        }
        if (playerOpenMenuInterval > 0 && --playerOpenMenuInterval == 0 && occupant instanceof ServerPlayer serverPlayer) {
            Optional<AugmentationRecipe> available = getRecipe();
            PacketDistributor.sendToPlayer(serverPlayer, new OpenAugmentationScreenPayload(worldPosition,
                    available.map(AugmentationRecipe::resultAugment),
                    available.map(value -> value.augmentItem().getDefaultInstance()).orElse(ItemStack.EMPTY)));
        }
    }

    private List<ItemStack> collectInputItems() {
        augmentItems.clear();
        List<ItemStack> items = new ArrayList<>();
        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            BlockPos pos = worldPosition.relative(direction, 2);
            var extension = (AugmentationStationExtensionBlockEntity) level.getBlockEntity(pos);
            ItemStack augmentItem = extension.getAugmentItem();
            if (!augmentItem.isEmpty()) {
                augmentItems.put(pos, augmentItem);
                items.add(augmentItem);
            }
        }
        return items;
    }

    private boolean isFormed() {
        return getBlockState().hasProperty(Multiblock.FORMED) && getBlockState().getValue(Multiblock.FORMED);
    }

    @Override
    public void setRemoved() {
        cancelAugmentation();
        super.setRemoved();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public MultiblockData getMultiblockData() {
        return multiblockData;
    }

    @Override
    public void setMultiblockData(MultiblockData data) {
        this.multiblockData = data;
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.multiblockData = loadMBData(in.read("multiblockData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.store("multiblockData", CompoundTag.CODEC, saveMBData());
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        cancelAugmentation();
        if (MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
            try {
                MultiblockHelper.unform(NTMultiblocks.AUGMENTATION_STATION.get(), pos, level);
            } finally {
                MultiblockEntity.UNFORMING.set(false);
            }
        }
        super.preRemoveSideEffects(pos, state);
        drop();
    }
}
