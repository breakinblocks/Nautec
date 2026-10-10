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
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.ResourceHandlerUtil;
import com.breakinblocks.nautec.transfer.resource.ResourceStack;
import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
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
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        IFluidHandlerItem item = carried.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        ResourceHandler<FluidResource> held = TransferCapabilities.wrapFluids(item);
        if (item == null || held == null) {
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
        ItemStack result = item.getContainer();
        if (carried.getCount() == 1) {
            setCarried(result);
        } else {
            carried.shrink(1);
            setCarried(carried);
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
        }
        triggerSoundAndGameEvent(moved.resource(), player.level(), Vec3.atCenterOf(blockEntity.getBlockPos()), player, pickup);
        return true;
    }

    private static void triggerSoundAndGameEvent(FluidResource resource, Level level, Vec3 position, Player player, boolean pickup) {
        FluidStack stack = resource.toStack(FluidType.BUCKET_VOLUME);
        SoundEvent soundEvent = resource.getFluidType().getSound(stack, pickup ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
        if (soundEvent != null) {
            level.playSound(null, position.x, position.y, position.z, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        level.gameEvent(player, pickup ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE, position);
    }
}
