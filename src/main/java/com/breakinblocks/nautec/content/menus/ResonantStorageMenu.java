package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.resonantstorage.ChannelAccess;
import com.breakinblocks.nautec.content.resonantstorage.FaceMode;
import com.breakinblocks.nautec.content.resonantstorage.ResonantAccess;
import com.breakinblocks.nautec.content.resonantstorage.ResonantLink;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStorageBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStore;
import com.breakinblocks.nautec.content.resonantstorage.UpgradeSlot;
import com.breakinblocks.nautec.network.ServerPacketGuards;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.item.StackCopySlot;
import org.jetbrains.annotations.Nullable;

public abstract class ResonantStorageMenu<T extends ResonantStorageBlockEntity> extends AbstractContainerMenu {
    public static final int ACCESS_BUTTON = 10;
    public static final int FACE_BUTTON = 100;
    public static final int ADDRESS_BUTTON = 1000;
    public static final int INVENTORY_Y = 132;
    public static final int UPGRADE_X = 152;
    public static final int UPGRADE_Y = 4;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int EDIT_THROTTLE = 2;

    public final T blockEntity;
    private final ContainerLevelAccess access;
    protected int playerSlotStart;
    protected int upgradeSlotIndex = -1;
    private boolean showsStorage = true;

    protected ResonantStorageMenu(MenuType<?> type, int containerId, T blockEntity) {
        super(type, containerId);
        this.blockEntity = blockEntity;
        this.access = blockEntity.getLevel() != null ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()) : ContainerLevelAccess.NULL;
    }

    protected abstract @Nullable ResonantStore store();

    protected void addUpgradeSlot() {
        upgradeSlotIndex = slots.size();
        addSlot(new ExpansionSlot(this, UPGRADE_X, UPGRADE_Y));
    }

    protected void addPlayerSlots(Inventory inventory) {
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

    public boolean showsStorage() {
        return showsStorage;
    }

    public void setShowsStorage(boolean showsStorage) {
        this.showsStorage = showsStorage;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && blockEntity.canUse(player)
                && stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer server)) {
            return true;
        }
        if (id >= ADDRESS_BUTTON && id < ADDRESS_BUTTON + GatewayAddress.addressCount()) {
            if (!ServerPacketGuards.allow(server, "resonant_storage", EDIT_THROTTLE)) {
                return false;
            }
            GatewayAddress address = GatewayAddress.unpack(id - ADDRESS_BUTTON);
            blockEntity.link(new ResonantLink(blockEntity.channel().withAddress(address), blockEntity.ownerName()));
            recoded(server);
            return true;
        }
        if (id >= ACCESS_BUTTON && id < ACCESS_BUTTON + ChannelAccess.ALL.length) {
            if (!ServerPacketGuards.allow(server, "resonant_storage", EDIT_THROTTLE)) {
                return false;
            }
            ChannelAccess wanted = ChannelAccess.byId(id - ACCESS_BUTTON);
            ResonantLink link = ResonantAccess.key(server, wanted, blockEntity.channel().address());
            if (link == null) {
                server.sendSystemMessage(Component.translatable("nautec.resonant_storage.no_team"), true);
                return false;
            }
            blockEntity.link(link);
            recoded(server);
            return true;
        }
        if (id >= FACE_BUTTON && id < FACE_BUTTON + DIRECTIONS.length * FaceMode.ALL.length) {
            if (!ServerPacketGuards.allow(server, "resonant_storage", EDIT_THROTTLE)) {
                return false;
            }
            int offset = id - FACE_BUTTON;
            blockEntity.setFace(DIRECTIONS[offset / FaceMode.ALL.length], FaceMode.byId(offset % FaceMode.ALL.length));
            return true;
        }
        return false;
    }

    private void recoded(ServerPlayer player) {
        MachineSounds.play(player.level(), blockEntity.getBlockPos(), NTSounds.RESONANT_STORAGE_RECODE, 0.8f, 1.0f);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        if (index == upgradeSlotIndex) {
            ResonantStore current = store();
            int removable = current != null ? current.upgradeSlot().removable() : 0;
            if (removable <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack taken = slot.remove(removable);
            if (!moveItemStackTo(taken, playerSlotStart, playerSlotStart + 36, true)) {
                slot.set(slot.getItem().copyWithCount(slot.getItem().getCount() + taken.getCount()));
                return ItemStack.EMPTY;
            }
            if (!taken.isEmpty()) {
                player.getInventory().placeItemBackInInventory(taken);
            }
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index >= playerSlotStart) {
            if (upgradeSlotIndex >= 0 && stack.is(NTItems.RESONANT_EXPANSION.get())
                    && moveItemStackTo(stack, upgradeSlotIndex, upgradeSlotIndex + 1, false)) {
                moved = true;
            } else {
                moved = moveIntoStorage(stack);
            }
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

    protected boolean moveIntoStorage(ItemStack stack) {
        return false;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return NTAbstractContainerMenu.mergeItemStack(slots, stack, startIndex, endIndex, reverseDirection);
    }

    static final class ExpansionSlot extends StackCopySlot {
        private final ResonantStorageMenu<?> menu;

        ExpansionSlot(ResonantStorageMenu<?> menu, int x, int y) {
            super(0, x, y);
            this.menu = menu;
        }

        private @Nullable UpgradeSlot upgrades() {
            ResonantStore store = menu.store();
            return store != null ? store.upgradeSlot() : null;
        }

        @Override
        protected ItemStack getStackCopy() {
            UpgradeSlot upgrades = upgrades();
            return upgrades == null ? ItemStack.EMPTY : upgrades.getResource(0).toStack(upgrades.getAmountAsInt(0));
        }

        @Override
        protected void setStackCopy(ItemStack stack) {
            UpgradeSlot upgrades = upgrades();
            if (upgrades != null) {
                upgrades.set(0, ItemResource.of(stack), stack.getCount());
            }
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(NTItems.RESONANT_EXPANSION.get());
        }

        @Override
        public boolean mayPickup(Player player) {
            UpgradeSlot upgrades = upgrades();
            return upgrades != null && upgrades.removable() > 0;
        }

        @Override
        public ItemStack remove(int amount) {
            UpgradeSlot upgrades = upgrades();
            int allowed = upgrades == null ? 0 : Math.min(amount, upgrades.removable());
            return allowed <= 0 ? ItemStack.EMPTY : super.remove(allowed);
        }

        @Override
        public int getMaxStackSize() {
            return ResonantStore.MAX_UPGRADES;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return ResonantStore.MAX_UPGRADES;
        }

        @Override
        public boolean isActive() {
            return menu.showsStorage();
        }
    }
}
