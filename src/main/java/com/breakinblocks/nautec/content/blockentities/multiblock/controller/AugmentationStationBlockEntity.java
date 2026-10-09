package com.breakinblocks.nautec.content.blockentities.multiblock.controller;

import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.network.AugmentationStationSyncPayload;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
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
import java.util.function.Supplier;
import com.google.common.base.Suppliers;

public class AugmentationStationBlockEntity extends ContainerBlockEntity implements MultiblockEntity {
    private final BeamOverclock overclock = new BeamOverclock();

    public static final int STATUS_READY = 0;
    public static final int STATUS_MISSING_EXTENSION = 1;
    public static final int STATUS_NO_ARM = 2;
    public static final int STATUS_EMPTY = 3;
    public static final int STATUS_NO_RECIPE = 4;
    public static final int STATUS_LOW_POWER = 5;
    public static final int STATUS_RUNNING = 6;
    public static final int STATUS_INSTALLED = 7;
    public static final int OPERATION_TICKS = 80;
    private static final int SYNC_INTERVAL = 10;
    private static final int INSTALLED_TICKS = 60;

    private MultiblockData multiblockData;
    private UUID playerUUID;
    private int playerOpenMenuInterval;

    private final Map<BlockPos, ItemStack> augmentItems;

    private boolean isRunning;
    private int duration;
    private Player player;
    private AugmentationRecipe recipe;
    private AugmentSlot slot;
    private int installedTicks;

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
        this.duration = OPERATION_TICKS;
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
            float speed = Float.MAX_VALUE;
            for (var input : operationInputs.entrySet()) {
                var extension = (AugmentationStationExtensionBlockEntity) level.getBlockEntity(input.getKey());
                if (extension.getPower() < NTConfig.augmentationStationPower
                        || !ItemStack.matches(input.getValue(), extension.getAugmentItem())) {
                    cancelAugmentation();
                    return;
                }
                speed = Math.min(speed, extension.beamSpeed());
            }
            duration -= overclock.advance(speed == Float.MAX_VALUE ? 1F : speed);
            if (duration <= 0) {
                Player recipient = player;
                AugmentationRecipe completedRecipe = recipe;
                AugmentSlot completedSlot = slot;
                List<ItemStack> parts = List.copyOf(operationInputs.values());
                for (BlockPos pos : operationInputs.keySet()) {
                    var extension = (AugmentationStationExtensionBlockEntity) level.getBlockEntity(pos);
                    extension.getItemStackHandler().extractItem(0, 1, false);
                }
                cancelAugmentation();
                AugmentHelper.createAugment(completedRecipe.resultAugment(), recipient, completedSlot, parts);
                this.installedTicks = INSTALLED_TICKS;
                if (recipient instanceof ServerPlayer serverPlayer) {
                    sendStatus(serverPlayer, false);
                }
            } else if (duration % SYNC_INTERVAL == 0 && player instanceof ServerPlayer serverPlayer) {
                sendStatus(serverPlayer, false);
            }
            return;
        }
        if (installedTicks > 0) {
            installedTicks--;
        }
        if (!isFormed()) {
            playerUUID = null;
            if (level.getGameTime() % 40 == 0) {
                for (Player standing : level.getEntitiesOfClass(Player.class, new AABB(worldPosition.above()))) {
                    standing.sendOverlayMessage(Component.translatable("nautec.augmentation_station.unformed").withStyle(ChatFormatting.RED));
                }
            }
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
        if (!(occupant instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (playerOpenMenuInterval > 0) {
            if (--playerOpenMenuInterval == 0) {
                sendStatus(serverPlayer, true);
            }
        } else if (level.getGameTime() % SYNC_INTERVAL == 0) {
            sendStatus(serverPlayer, false);
        }
    }

    public int getStatus() {
        return getStatus(this::getRecipe);
    }

    private int getStatus(Supplier<Optional<AugmentationRecipe>> recipeLookup) {
        if (isRunning) {
            return STATUS_RUNNING;
        }
        if (installedTicks > 0) {
            return STATUS_INSTALLED;
        }
        if (!isFormed() || !extensionsPresent()) {
            return STATUS_MISSING_EXTENSION;
        }
        boolean loaded = false;
        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            var extension = (AugmentationStationExtensionBlockEntity) level.getBlockEntity(worldPosition.relative(direction, 2));
            if (extension.getItemStackHandler().getStackInSlot(AugmentationStationExtensionBlockEntity.AUGMENT_SLOT).isEmpty()) {
                continue;
            }
            if (!hasArm(extension)) {
                return STATUS_NO_ARM;
            }
            loaded = true;
        }
        if (!loaded) {
            return STATUS_EMPTY;
        }
        if (recipeLookup.get().isEmpty()) {
            return STATUS_NO_RECIPE;
        }
        for (BlockPos pos : augmentItems.keySet()) {
            if (!(level.getBlockEntity(pos) instanceof AugmentationStationExtensionBlockEntity extension)
                    || extension.getPower() < NTConfig.augmentationStationPower) {
                return STATUS_LOW_POWER;
            }
        }
        return STATUS_READY;
    }

    private static boolean hasArm(AugmentationStationExtensionBlockEntity extension) {
        return extension.getItemStackHandler().getStackInSlot(AugmentationStationExtensionBlockEntity.ROBOT_ARM_SLOT).is(NTItems.CLAW_ROBOT_ARM);
    }

    private void sendStatus(ServerPlayer player, boolean open) {
        Supplier<Optional<AugmentationRecipe>> recipeLookup = Suppliers.memoize(this::getRecipe);
        int status = getStatus(recipeLookup);
        Optional<AugmentationRecipe> available = status == STATUS_RUNNING ? Optional.ofNullable(recipe) : recipeLookup.get();
        List<AugmentationStationSyncPayload.Extension> extensions = new ArrayList<>();
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            BlockPos pos = worldPosition.relative(direction, 2);
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof AugmentationStationExtensionBlockEntity extension
                    && extension.getBlockState().getValue(Multiblock.FORMED)) {
                extensions.add(new AugmentationStationSyncPayload.Extension(direction, true, hasArm(extension),
                        extension.getItemStackHandler().getStackInSlot(AugmentationStationExtensionBlockEntity.AUGMENT_SLOT).copy(),
                        extension.getPower()));
            } else {
                extensions.add(new AugmentationStationSyncPayload.Extension(direction, false, false, ItemStack.EMPTY, 0));
            }
        }
        PacketDistributor.sendToPlayer(player, new AugmentationStationSyncPayload(worldPosition, open, status,
                status == STATUS_RUNNING ? OPERATION_TICKS - duration : 0,
                available.map(AugmentationRecipe::resultAugment),
                available.map(value -> value.augmentItem().getDefaultInstance()).orElse(ItemStack.EMPTY),
                available.map(AugmentationRecipe::desc).orElse(""),
                extensions));
    }

    private List<ItemStack> collectInputItems() {
        augmentItems.clear();
        List<ItemStack> items = new ArrayList<>();
        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            BlockPos pos = worldPosition.relative(direction, 2);
            if (!(level.getBlockEntity(pos) instanceof AugmentationStationExtensionBlockEntity extension)) {
                continue;
            }
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
