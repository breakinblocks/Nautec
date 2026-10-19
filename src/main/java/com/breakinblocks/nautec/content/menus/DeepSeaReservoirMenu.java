package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirBlockEntity;
import com.breakinblocks.nautec.content.reservoir.ReservoirTank;
import com.breakinblocks.nautec.content.reservoir.ReservoirText;
import com.breakinblocks.nautec.content.reservoir.ReservoirUpgradeItem;
import com.breakinblocks.nautec.network.ServerPacketGuards;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.ResourceHandlerUtil;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.TransferCapabilities;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import com.breakinblocks.nautec.transfer.resource.ResourceStack;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

public class DeepSeaReservoirMenu extends AbstractContainerMenu {
    public static final int REMOVE_BUTTON = 1;
    public static final int GAUGE_BUTTON = 2;
    public static final int UPGRADE_X = 62;
    public static final int UPGRADE_Y = 80;
    public static final int INVENTORY_Y = 108;

    public final DeepSeaReservoirBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final boolean client;
    private final SimpleContainer upgradeInput = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            applyUpgrade();
        }
    };
    private final int playerSlotStart;

    public DeepSeaReservoirMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, clientReservoir(inventory, extraData.readBlockPos()), true);
    }

    public DeepSeaReservoirMenu(int containerId, Inventory inventory, DeepSeaReservoirBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity, false);
    }

    private DeepSeaReservoirMenu(int containerId, Inventory inventory, DeepSeaReservoirBlockEntity blockEntity, boolean client) {
        super(NTMenuTypes.DEEP_SEA_RESERVOIR.get(), containerId);
        this.blockEntity = blockEntity;
        this.client = client;
        this.access = blockEntity.getLevel() != null ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()) : ContainerLevelAccess.NULL;
        addSlot(new UpgradeSlot(upgradeInput, UPGRADE_X, UPGRADE_Y));
        playerSlotStart = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, INVENTORY_Y + 58));
        }
    }

    private static DeepSeaReservoirBlockEntity clientReservoir(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof DeepSeaReservoirBlockEntity found
                ? found
                : new DeepSeaReservoirBlockEntity(pos, NTBlocks.DEEP_SEA_RESERVOIR.get().defaultBlockState());
    }

    public static Item upgradeFor(int tier) {
        return switch (tier) {
            case 2 -> NTItems.RESERVOIR_UPGRADE_2.get();
            case 3 -> NTItems.RESERVOIR_UPGRADE_3.get();
            default -> NTItems.RESERVOIR_UPGRADE_4.get();
        };
    }

    public boolean acceptsUpgrade(ItemStack stack) {
        return stack.getItem() instanceof ReservoirUpgradeItem upgrade && upgrade.tier() == blockEntity.tier() + 1;
    }

    private void applyUpgrade() {
        if (client || blockEntity.isRemoved()) {
            return;
        }
        ItemStack stack = upgradeInput.getItem(0);
        if (stack.isEmpty() || !acceptsUpgrade(stack)) {
            return;
        }
        int tier = ((ReservoirUpgradeItem) stack.getItem()).tier();
        upgradeInput.removeItemNoUpdate(0);
        blockEntity.upgradeTo(tier);
        if (blockEntity.getLevel() != null) {
            blockEntity.getLevel().playSound(null, blockEntity.getBlockPos(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.8f, 0.9f + 0.1f * tier);
        }
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer server)) {
            return true;
        }
        if (!ServerPacketGuards.allow(server, "deep_sea_reservoir", 2)) {
            return false;
        }
        if (id == REMOVE_BUTTON) {
            return removeUpgrade(server);
        }
        if (id == GAUGE_BUTTON) {
            return gauge(server);
        }
        return false;
    }

    private boolean removeUpgrade(ServerPlayer player) {
        int tier = blockEntity.tier();
        if (tier <= 1) {
            return false;
        }
        long voided = blockEntity.downgrade();
        player.getInventory().placeItemBackInInventory(new ItemStack(upgradeFor(tier)));
        if (voided > 0) {
            player.displayClientMessage(Component.translatable("nautec.deep_sea_reservoir.voided", ReservoirText.buckets(voided))
                    .withStyle(ChatFormatting.GOLD), true);
        }
        if (blockEntity.getLevel() != null) {
            blockEntity.getLevel().playSound(null, blockEntity.getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.4f, 1.4f);
        }
        return true;
    }

    private boolean gauge(ServerPlayer player) {
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        IFluidHandlerItem item = carried.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        ResourceHandler<FluidResource> held = TransferCapabilities.wrapFluids(item);
        if (item == null || held == null) {
            return false;
        }
        ReservoirTank tank = blockEntity.tank();
        boolean pickup = true;
        ResourceStack<FluidResource> moved;
        try (Transaction transaction = Transaction.openRoot()) {
            moved = ResourceHandlerUtil.moveFirst(tank, held, resource -> true, Integer.MAX_VALUE, transaction);
            if (moved == null) {
                pickup = false;
                moved = ResourceHandlerUtil.moveFirst(held, tank, resource -> true, Integer.MAX_VALUE, transaction);
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
        Vec3 position = Vec3.atCenterOf(blockEntity.getBlockPos());
        FluidStack sample = moved.resource().toStack(FluidType.BUCKET_VOLUME);
        SoundEvent sound = moved.resource().getFluidType().getSound(sample, pickup ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
        if (sound != null) {
            player.level().playSound(null, position.x, position.y, position.z, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        player.level().gameEvent(player, pickup ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE, position);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && stillValid(access, player, NTBlocks.DEEP_SEA_RESERVOIR.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!client) {
            clearContainer(player, upgradeInput);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index >= playerSlotStart) {
            moved = acceptsUpgrade(stack) && moveItemStackTo(stack, 0, 1, false);
        } else {
            moved = moveItemStackTo(stack, playerSlotStart, playerSlotStart + 36, true);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return NTAbstractContainerMenu.mergeItemStack(slots, stack, startIndex, endIndex, reverseDirection);
    }

    private final class UpgradeSlot extends Slot {
        private UpgradeSlot(SimpleContainer container, int x, int y) {
            super(container, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return acceptsUpgrade(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }
}
