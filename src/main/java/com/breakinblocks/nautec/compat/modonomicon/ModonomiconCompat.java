package com.breakinblocks.nautec.compat.modonomicon;

import com.klikli_dev.modonomicon.data.BookDataManager;
import com.klikli_dev.modonomicon.registry.DataComponentRegistry;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ModonomiconCompat {
    public static ItemStack getItemStack() {
        Identifier id = BookDataManager.get().getBook(Nautec.rl("nautec_guide")).getId();
        ItemStack itemStack = new ItemStack(NTItems.NAUTEC_GUIDE.get());
        itemStack.set(DataComponentRegistry.BOOK_ID.get(), id);
        return itemStack;
    }

    public static Supplier<Item> registerItem() {
        return NTItems.registerItem("nautec_guide", props -> (Item) new NautecGuideItem(props.stacksTo(1)), new Item.Properties());
    }
}
