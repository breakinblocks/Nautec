package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.CrystalCultivatorBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.tags.NTTags;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.GameRules;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CrystalCultivatorBlockEntity extends LaserBlockEntity {
    private static final int SLOTS = 9;
    private static final int INTERVAL = 10;
    private static final SlotRoles ROLES = SlotRoles.of(new int[0], SlotRoles.range(0, SLOTS));
    private static final GameProfile HARVESTER = new GameProfile(
            UUID.nameUUIDFromBytes("nautec:crystal_cultivator".getBytes(StandardCharsets.UTF_8)), "NauTecCultivator");
    private double growthCredit;
    private int ticks;

    public CrystalCultivatorBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.CRYSTAL_CULTIVATOR.get(), pos, state);
        addItemHandler(SLOTS, (slot, stack) -> false);
        ticks = Math.floorMod(pos.hashCode(), INTERVAL);
    }

    public Direction facing() {
        return getBlockState().getValue(CrystalCultivatorBlock.FACING);
    }

    public BlockPos sourcePos() {
        return worldPosition.relative(facing().getOpposite());
    }

    public static double growthSpeed(int power) {
        if (power < 20) return 0;
        if (power <= 100) return 1 + (power - 20) * 9.0 / 80;
        return 10 + 9 * Math.log1p((power - 100) / 80.0);
    }

    public static int fortune(float purity) {
        if (!Float.isFinite(purity) || purity <= 0) return 0;
        return (int) Math.min(255, Math.floor(purity <= 6 ? purity : 6 + 3 * Math.log1p((purity - 6) / 3.0)));
    }

    @Override
    public int getRequiredPower() {
        return 20;
    }

    @Override
    public float beamSpeed() {
        return (float) growthSpeed(getPower());
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return Set.of(facing());
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public SlotRoles itemRoles() {
        return ROLES;
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    public int storedItems() {
        int total = 0;
        for (int slot = 0; slot < SLOTS; slot++) total += getItemStackHandler().getStackInSlot(slot).getCount();
        return total;
    }

    // A stable owner prevents multiple attachments multiplying the growth rate.
    public boolean controlsSource() {
        BlockPos source = sourcePos();
        for (Direction side : Direction.values()) {
            BlockPos other = source.relative(side);
            if (other.asLong() >= worldPosition.asLong() || !level.isLoaded(other)) continue;
            if (level.getBlockEntity(other) instanceof CrystalCultivatorBlockEntity cultivator
                    && cultivator.sourcePos().equals(source)) return false;
        }
        return true;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel server)) return;
        BlockPos source = sourcePos();
        boolean running = getPower() >= 20 && server.isLoaded(source)
                && server.getBlockState(source).is(NTTags.Blocks.CULTIVATOR_SOURCES) && controlsSource();
        if (getBlockState().getValue(CrystalCultivatorBlock.ACTIVE) != running) {
            server.setBlock(worldPosition, getBlockState().setValue(CrystalCultivatorBlock.ACTIVE, running), Block.UPDATE_CLIENTS);
        }
        if (!running) {
            growthCredit = 0;
            return;
        }
        if (++ticks % INTERVAL != 0) return;
        for (Direction side : Direction.values()) harvest(server, source.relative(side), side);

        // Vanilla selects random block positions from 4096 cells in each section.
        // Natural ticks supply 1x; only add the requested excess, respecting the gamerule.
        int randomTicks = server.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        growthCredit = Math.min(64, growthCredit + (growthSpeed(getPower()) - 1) * randomTicks * INTERVAL / 4096.0);
        while (growthCredit >= 1) {
            growthCredit--;
            BlockState state = server.getBlockState(source);
            if (!state.is(NTTags.Blocks.CULTIVATOR_SOURCES) || !state.isRandomlyTicking()) break;
            state.randomTick(server, source, server.getRandom());
        }
        setChanged();
    }

    public boolean harvest(ServerLevel server, BlockPos pos, Direction outward) {
        if (getPower() < 20 || !server.isLoaded(pos) || !pos.equals(sourcePos().relative(outward))) return false;
        if (!server.getBlockState(sourcePos()).is(NTTags.Blocks.CULTIVATOR_SOURCES) || !controlsSource()) return false;
        BlockState state = server.getBlockState(pos);
        if (!state.is(NTTags.Blocks.CULTIVATOR_CLUSTERS) || server.getBlockEntity(pos) != null) return false;
        if (state.hasProperty(BlockStateProperties.FACING) && state.getValue(BlockStateProperties.FACING) != outward) return false;
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        int fortune = fortune(getPurity());
        if (fortune > 0) tool.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), fortune);
        var player = FakePlayerFactory.get(server, HARVESTER);
        ItemStack previousTool = player.getMainHandItem();
        var previousPosition = player.position();
        List<ItemStack> drops;
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);
            player.setPos(worldPosition.getCenter());
            drops = Block.getDrops(state, server, pos, null, player, tool);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, previousTool);
            player.setPos(previousPosition);
        }
        if (drops.isEmpty()) return false;
        List<ItemStack> buffer = new ArrayList<>(SLOTS);
        for (int slot = 0; slot < SLOTS; slot++) buffer.add(getItemStackHandler().getStackInSlot(slot).copy());
        for (ItemStack drop : drops) {
            int remaining = drop.getCount();
            for (int slot = 0; slot < SLOTS && remaining > 0; slot++) {
                ItemStack stack = buffer.get(slot);
                if (!stack.isEmpty() && !ItemStack.isSameItemSameComponents(stack, drop)) continue;
                int inserted = Math.min(remaining, Math.min(64, drop.getMaxStackSize()) - stack.getCount());
                if (inserted <= 0) continue;
                buffer.set(slot, drop.copyWithCount(stack.getCount() + inserted));
                remaining -= inserted;
            }
            if (remaining > 0) return false;
        }
        // Commit only once every drop fits. Waterlogged clusters leave their water behind.
        if (!server.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL)) return false;
        for (int slot = 0; slot < SLOTS; slot++) getItemStackHandler().setStackInSlot(slot, buffer.get(slot));
        server.levelEvent(2001, pos, Block.getId(state));
        return true;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putDouble("growth_credit", growthCredit);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        growthCredit = Math.clamp(in.getDoubleOr("growth_credit", 0), 0, 1);
    }
}
