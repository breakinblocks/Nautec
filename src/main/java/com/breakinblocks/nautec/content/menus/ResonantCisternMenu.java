package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.content.resonantstorage.CisternStore;
import com.breakinblocks.nautec.content.resonantstorage.ResonantCisternBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStore;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class ResonantCisternMenu extends ResonantStorageMenu<ResonantCisternBlockEntity> {
    public static final int GAUGE_BUTTON = 200;
    private final @Nullable CisternStore mirror;

    public ResonantCisternMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, clientCistern(inventory, extraData), true);
    }

    public ResonantCisternMenu(int containerId, Inventory inventory, ResonantCisternBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity, false);
    }

    private ResonantCisternMenu(int containerId, Inventory inventory, ResonantCisternBlockEntity blockEntity, boolean client) {
        super(NTMenuTypes.RESONANT_CISTERN.get(), containerId, blockEntity);
        this.mirror = client ? new CisternStore(blockEntity.channel()) : null;
        addUpgradeSlot();
        addPlayerSlots(inventory);
        if (!client) {
            MachineSounds.play(blockEntity.getLevel(), blockEntity.getBlockPos(), NTSounds.RESONANT_CISTERN_OPEN, 0.6f, 1.0f);
        }
    }

    private static ResonantCisternBlockEntity clientCistern(Inventory inventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        return inventory.player.level().getBlockEntity(pos) instanceof ResonantCisternBlockEntity found
                ? found
                : new ResonantCisternBlockEntity(pos, NTBlocks.RESONANT_CISTERN.get().defaultBlockState());
    }

    @Override
    protected @Nullable ResonantStore store() {
        return mirror != null ? mirror : blockEntity.cistern();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != GAUGE_BUTTON) {
            return super.clickMenuButton(player, id);
        }
        if (!(player instanceof ServerPlayer)) {
            return true;
        }
        CisternStore cistern = blockEntity.cistern();
        if (cistern == null) {
            return false;
        }
        ItemAccess cursor = ItemAccess.forPlayerCursor(player, this).oneByOne();
        ResourceHandler<FluidResource> held = cursor.getCapability(Capabilities.Fluid.ITEM);
        if (held == null) {
            return false;
        }
        boolean pickup = true;
        ResourceStack<FluidResource> moved;
        try (Transaction transaction = Transaction.openRoot()) {
            moved = ResourceHandlerUtil.moveFirst(cistern, held, resource -> true, Integer.MAX_VALUE, transaction);
            if (moved == null) {
                pickup = false;
                moved = ResourceHandlerUtil.moveFirst(held, cistern, resource -> true, Integer.MAX_VALUE, transaction);
            }
            transaction.commit();
        }
        if (moved == null) {
            return false;
        }
        FluidUtil.triggerSoundAndGameEvent(moved.resource(), player.level(), Vec3.atCenterOf(blockEntity.getBlockPos()), player, pickup);
        return true;
    }
}
