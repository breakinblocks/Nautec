package com.breakinblocks.nautec.content.items.tools;

import com.breakinblocks.nautec.content.conduits.ConduitWrenching;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockPartEntity;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.sides.RelativeFace;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.api.sides.SideMode;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.BioReactorPartBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.IndustrialBioReactorPartBlockEntity;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.IndustrialBioReactorMultiblock;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.utils.BlockUtils;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class AquarineWrenchItem extends Item {
    public AquarineWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    public static WrenchMode mode(ItemStack stack) {
        return WrenchMode.byId(stack.getOrDefault(NTDataComponents.WRENCH_MODE.get(), 0));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        WrenchMode next = mode(stack).next();
        stack.set(NTDataComponents.WRENCH_MODE.get(), next.ordinal());
        if (!level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable("nautec.wrench.mode", Component.translatable(next.translationKey())).withStyle(ChatFormatting.AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("nautec.wrench.mode", Component.translatable(mode(stack).translationKey())).withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.translatable(mode(stack).translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("nautec.wrench.mode.switch").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos pos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(pos);
        BlockState controllerState = blockState;
        Player player = useOnContext.getPlayer();

        if (DrainPartBlock.wrenchLaserPort(level, pos, blockState, player)) {
            return InteractionResult.SUCCESS;
        }

        InteractionResult conduit = ConduitWrenching.use(useOnContext);
        if (conduit != null) {
            return conduit;
        }

        if (level.getBlockEntity(pos) instanceof LaserJunctionBlockEntity be && blockState.hasProperty(LaserJunctionBlock.CONNECTION[0])) {
            Direction direction = useOnContext.getClickedFace();
            LaserJunctionBlock.ConnectionType newType = switch (blockState.getValue(LaserJunctionBlock.CONNECTION[direction.ordinal()])) {
                case INPUT ->
                        player.isShiftKeyDown() ? LaserJunctionBlock.ConnectionType.OUTPUT : LaserJunctionBlock.ConnectionType.NONE;
                case OUTPUT ->
                        player.isShiftKeyDown() ? LaserJunctionBlock.ConnectionType.NONE : LaserJunctionBlock.ConnectionType.INPUT;
                case NONE ->
                        player.isShiftKeyDown() ? LaserJunctionBlock.ConnectionType.OUTPUT : LaserJunctionBlock.ConnectionType.INPUT;
            };
            level.setBlockAndUpdate(pos, blockState.setValue(LaserJunctionBlock.CONNECTION[direction.ordinal()], newType));
            switch (newType) {
                case INPUT -> {
                    be.getLaserInputs().add(direction);
                    be.getLaserOutputs().remove(direction);
                }
                case OUTPUT -> {
                    be.getLaserInputs().remove(direction);
                    be.getLaserOutputs().add(direction);
                }
                case NONE -> {
                    be.getLaserInputs().remove(direction);
                    be.getLaserOutputs().remove(direction);
                }
            }

            return InteractionResult.SUCCESS;
        }

        WrenchMode mode = mode(useOnContext.getItemInHand());
        SideKind kind = mode.kind();
        if (kind != null && sideConfigTarget(level, pos) instanceof ContainerBlockEntity machine && machine.hasSideConfig(kind)) {
            if (!level.isClientSide() && player != null) {
                RelativeFace face = RelativeFace.of(machine.front(), useOnContext.getClickedFace());
                SideMode current = machine.getSideConfig().get(kind, face);
                SideMode next = player.isSecondaryUseActive() ? current.previous() : current.next();
                machine.setSideMode(kind, face, next);
                player.sendOverlayMessage(Component.translatable("nautec.side_config.tooltip", Component.translatable(face.translationKey()),
                        Component.translatable(kind.translationKey()), Component.translatable(next.translationKey())).withStyle(ChatFormatting.AQUA));
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.6F, 1.4F);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof BioReactorPartBlockEntity partBE) {
            BlockState state = level.getBlockState(pos);
            if (state.getValue(BioReactorMultiblock.TOP) && state.getValue(BioReactorMultiblock.BIO_REACTOR_PART) % 2 != 0) {
                boolean hatch = !state.getValue(BioReactorMultiblock.HATCH);
                level.setBlockAndUpdate(pos, state.setValue(BioReactorMultiblock.HATCH, hatch));
                partBE.setLaserInput(hatch);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        if (level.getBlockEntity(pos) instanceof IndustrialBioReactorPartBlockEntity industrialPart) {
            BlockState state = level.getBlockState(pos);
            if (IndustrialBioReactorMultiblock.isHatchCandidate(industrialPart.getLayer(), industrialPart.getCell())) {
                level.setBlockAndUpdate(pos, state.setValue(BioReactorMultiblock.HATCH, !state.getValue(BioReactorMultiblock.HATCH)));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        if (!useOnContext.getPlayer().isCrouching()) {
            for (Multiblock multiblock : NTRegistries.MULTIBLOCK) {
                if (controllerState.is(multiblock.getUnformedController())) {
                    if (controllerState.hasProperty(Multiblock.FORMED) && controllerState.getValue(Multiblock.FORMED)) {
                        break;
                    }
                    try {
                        if (MultiblockHelper.form(multiblock, pos, level, useOnContext.getPlayer())) {
                            return InteractionResult.SUCCESS;
                        }
                        break;
                    } catch (Exception e) {
                        Nautec.LOGGER.error("Encountered err forming multiblock", e);
                    }
                }
            }

            for (Property<?> prop : mode == WrenchMode.ROTATE ? blockState.getProperties() : List.<Property<?>>of()) {
                if (prop instanceof EnumProperty<?> enumProperty && enumProperty.getValueClass() == Direction.class && prop.getName().equals("facing")) {
                    @SuppressWarnings("unchecked")
                    EnumProperty<Direction> directionProperty = (EnumProperty<Direction>) enumProperty;
                    BlockState rotatedState = BlockUtils.rotateBlock(blockState, directionProperty, blockState.getValue(directionProperty));
                    level.setBlock(pos, rotatedState, 3);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.FAIL;
    }

    private static @Nullable BlockEntity sideConfigTarget(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MultiblockPartEntity part && part.getControllerPos() != null
                && level.getBlockEntity(part.getControllerPos()) instanceof AbstractBioReactorBlockEntity reactor) {
            return reactor;
        }
        return blockEntity;
    }
}
