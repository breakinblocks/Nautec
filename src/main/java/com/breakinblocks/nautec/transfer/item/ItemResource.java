package com.breakinblocks.nautec.transfer.item;

import com.breakinblocks.nautec.transfer.resource.DataComponentHolderResource;
import com.breakinblocks.nautec.utils.templates.ItemStackTemplate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ItemResource implements DataComponentHolderResource<Item> {
    private static final Map<Item, ItemResource> PLAIN = new ConcurrentHashMap<>();

    public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);

    public static final Codec<ItemResource> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(i -> i.group(
            ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemResource::typeHolder),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemResource::getComponentsPatch)
    ).apply(i, ItemResource::of)));

    public static final Codec<ItemResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(EMPTY),
            resource -> resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), ItemResource::typeHolder,
            DataComponentPatch.STREAM_CODEC, ItemResource::getComponentsPatch,
            ItemResource::of);

    private final ItemStack stack;

    private ItemResource(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemResource of(ItemStack stack) {
        if (stack.isEmpty()) {
            return EMPTY;
        }
        if (stack.isComponentsPatchEmpty()) {
            return of(stack.getItem());
        }
        return new ItemResource(stack.copyWithCount(1));
    }

    public static ItemResource of(@Nullable ItemStackTemplate template) {
        if (template == null) {
            return EMPTY;
        }
        return of(template.item(), template.components());
    }

    public static ItemResource of(ItemLike itemLike) {
        Item item = itemLike.asItem();
        if (item == ItemStack.EMPTY.getItem()) {
            return EMPTY;
        }
        return PLAIN.computeIfAbsent(item, key -> new ItemResource(new ItemStack(key)));
    }

    public static ItemResource of(ItemLike item, DataComponentPatch patch) {
        return of(item.asItem().builtInRegistryHolder(), patch);
    }

    public static ItemResource of(Holder<Item> holder) {
        return of(holder.value());
    }

    public static ItemResource of(Holder<Item> holder, DataComponentPatch patch) {
        if (patch.isEmpty()) {
            return of(holder.value());
        }
        ItemStack created = new ItemStack(holder, 1, patch);
        if (created.isEmpty()) {
            return EMPTY;
        }
        return new ItemResource(created);
    }

    @Override
    public Item value() {
        return stack.getItem();
    }

    public Item getItem() {
        return value();
    }

    @Override
    public Holder<Item> typeHolder() {
        return stack.getItemHolder();
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public boolean matches(ItemStack other) {
        return ItemStack.isSameItemSameComponents(stack, other);
    }

    public boolean matches(@Nullable ItemStackTemplate template) {
        return template == null ? isEmpty() : template.item().value() == value() && template.components().equals(getComponentsPatch());
    }

    public boolean is(ItemLike item) {
        return value() == item.asItem();
    }

    public boolean test(Predicate<ItemStack> predicate) {
        return predicate.test(stack);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return stack.isComponentsPatchEmpty();
    }

    @Override
    public ItemResource withMergedPatch(DataComponentPatch patch) {
        if (isEmpty() || patch.isEmpty()) {
            return this;
        }
        ItemStack copy = stack.copy();
        copy.applyComponents(patch);
        return of(copy);
    }

    @Override
    public <D> ItemResource with(DataComponentType<D> type, @Nullable D data) {
        if (isEmpty()) {
            return this;
        }
        ItemStack copy = stack.copy();
        copy.set(type, data);
        return of(copy);
    }

    @Override
    public <D> ItemResource with(Supplier<? extends DataComponentType<D>> type, @Nullable D data) {
        return with(type.get(), data);
    }

    @Override
    public ItemResource without(DataComponentType<?> type) {
        if (isEmpty()) {
            return this;
        }
        ItemStack copy = stack.copy();
        copy.remove(type);
        return of(copy);
    }

    @Override
    public ItemResource without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }

    @Override
    public DataComponentMap getComponents() {
        return stack.getComponents();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return stack.getComponentsPatch();
    }

    public ItemStack toStack(int count) {
        return isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(count);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public int getMaxStackSize() {
        return stack.getMaxStackSize();
    }

    public Component getHoverName() {
        return stack.getHoverName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof ItemResource other && ItemStack.isSameItemSameComponents(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }

    @Override
    public String toString() {
        return isEmpty() ? "ItemResource[EMPTY]" : "ItemResource[" + stack.getItem() + ", " + stack.getComponentsPatch() + "]";
    }
}
