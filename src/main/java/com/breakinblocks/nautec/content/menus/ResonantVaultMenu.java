package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.content.resonantstorage.ResonantStore;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.VaultStore;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.world.inventory.StackCopySlot;
import org.jetbrains.annotations.Nullable;

public class ResonantVaultMenu extends ResonantStorageMenu<ResonantVaultBlockEntity> {
    public static final int GRID_X = 8;
    public static final int GRID_Y = 34;
    private final @Nullable VaultStore mirror;
    private final DataSlot page = DataSlot.standalone();

    public ResonantVaultMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, clientVault(inventory, extraData), true);
    }

    public ResonantVaultMenu(int containerId, Inventory inventory, ResonantVaultBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity, false);
    }

    private ResonantVaultMenu(int containerId, Inventory inventory, ResonantVaultBlockEntity blockEntity, boolean client) {
        super(NTMenuTypes.RESONANT_VAULT.get(), containerId, blockEntity);
        this.mirror = client ? new VaultStore(blockEntity.channel()) : null;
        addDataSlot(page);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new PageSlot(this, column + row * 9, GRID_X + column * 18, GRID_Y + row * 18));
            }
        }
        addUpgradeSlot();
        addPlayerSlots(inventory);
        if (!client) {
            blockEntity.startOpen(inventory.player);
        }
    }

    private static ResonantVaultBlockEntity clientVault(Inventory inventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        return inventory.player.level().getBlockEntity(pos) instanceof ResonantVaultBlockEntity found
                ? found
                : new ResonantVaultBlockEntity(pos, NTBlocks.RESONANT_VAULT.get().defaultBlockState());
    }

    @Override
    protected @Nullable ResonantStore store() {
        return vault();
    }

    public @Nullable VaultStore vault() {
        return mirror != null ? mirror : blockEntity.vault();
    }

    public int page() {
        return page.get();
    }

    public int pages() {
        VaultStore vault = vault();
        return vault != null ? vault.pages() : 1;
    }

    public void selectPage(int index) {
        page.set(Math.max(0, Math.min(index, ResonantStore.MAX_UPGRADES)));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id <= ResonantStore.MAX_UPGRADES) {
            if (id < pages()) {
                selectPage(id);
            }
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    protected boolean moveIntoStorage(ItemStack stack) {
        VaultStore vault = vault();
        if (vault == null || stack.isEmpty()) {
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = ResourceHandlerUtil.insertStacking(vault.view(), ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
            stack.shrink(inserted);
            return inserted > 0;
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (mirror == null) {
            blockEntity.stopOpen(player);
        }
    }

    static final class PageSlot extends StackCopySlot {
        private final ResonantVaultMenu menu;
        private final int offset;

        PageSlot(ResonantVaultMenu menu, int offset, int x, int y) {
            super(offset, x, y);
            this.menu = menu;
            this.offset = offset;
        }

        private int index() {
            return menu.page() * VaultStore.PAGE + offset;
        }

        @Override
        protected ItemStack getStackCopy() {
            VaultStore vault = menu.vault();
            return vault == null ? ItemStack.EMPTY : vault.stackAt(index()).copy();
        }

        @Override
        protected void setStackCopy(ItemStack stack) {
            VaultStore vault = menu.vault();
            if (vault != null) {
                vault.setStackAt(index(), stack);
            }
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            VaultStore vault = menu.vault();
            return vault != null && !stack.isEmpty() && index() < vault.capacity();
        }

        @Override
        public int getMaxStackSize() {
            return 99;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return Math.min(99, stack.getMaxStackSize());
        }

        @Override
        public boolean isActive() {
            VaultStore vault = menu.vault();
            return menu.showsStorage() && vault != null && index() < vault.capacity();
        }
    }
}
