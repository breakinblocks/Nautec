package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BacteriaSlotClickedPayload(BlockPos pos, int containerId, int slot) implements CustomPacketPayload {
    public static final Type<BacteriaSlotClickedPayload> TYPE = new Type<>(Nautec.rl("insert_bacteria"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BacteriaSlotClickedPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BacteriaSlotClickedPayload::pos,
            ByteBufCodecs.VAR_INT, BacteriaSlotClickedPayload::containerId,
            ByteBufCodecs.VAR_INT, BacteriaSlotClickedPayload::slot,
            BacteriaSlotClickedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            if (level.isClientSide() || !player.isAlive() || player.isSpectator() || !level.isLoaded(pos)
                    || !(player.containerMenu instanceof NTMachineMenu<?> menu)
                    || menu.containerId != containerId || !menu.blockEntity.getBlockPos().equals(pos)
                    || level.getBlockEntity(pos) != menu.blockEntity || !menu.stillValid(player)
                    || !menu.getCarried().is(NTItems.PETRI_DISH)) {
                return;
            }

            IBacteriaStorage machine = menu.blockEntity.getBacteriaStorage();
            IBacteriaStorage carried = menu.getCarried().getCapability(NTCapabilities.BacteriaStorage.ITEM);
            if (machine == null || carried == null || slot < 0 || slot >= machine.getBacteriaSlots()
                    || menu.getBacteriaStorageSlots().stream().noneMatch(entry -> entry.getSlot() == slot
                    && entry.getBacteriaStorage() == machine)) {
                return;
            }

            BacteriaInstance held = carried.getBacteria(0);
            BacteriaInstance stored = machine.getBacteria(slot);
            if (held.isEmpty() || BacteriaInstance.isSameBacteriaAndStats(held, stored)) {
                BacteriaInstance remainder = carried.insertBacteria(0, stored, false);
                machine.setBacteria(slot, remainder);
                machine.onBacteriaChanged(slot);
            } else {
                BacteriaInstance remainder = machine.insertBacteria(slot, held, false);
                carried.setBacteria(0, remainder);
                carried.onBacteriaChanged(0);
            }
            menu.broadcastChanges();
        });
    }
}
