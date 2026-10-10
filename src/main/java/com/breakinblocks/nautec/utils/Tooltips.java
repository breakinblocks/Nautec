package com.breakinblocks.nautec.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public final class Tooltips {
    public static void trans(Consumer<Component> components, String key, ChatFormatting... formatings) {
        tt(components, Component.translatable(key), formatings);
    }

    public static void transInsert(Consumer<Component> components, String key, String additional, ChatFormatting... formatings) {
        MutableComponent trans = apply(Component.translatable(key), formatings);
        MutableComponent lit = apply(Component.literal(additional), formatings);
        components.accept(trans.append(lit));
    }

    public static void transtrans(Consumer<Component> components, String key, String additional, ChatFormatting... formatings) {
        MutableComponent trans = apply(Component.translatable(key), formatings);
        MutableComponent extra = apply(Component.translatable(additional), formatings);
        components.accept(trans.append(extra));
    }

    public static void tt(Consumer<Component> components, MutableComponent c, ChatFormatting... formatings) {
        apply(c, formatings);
        components.accept(c);
    }

    public static void trans(List<Component> components, String key, ChatFormatting... formatings) {
        trans(components::add, key, formatings);
    }

    public static void transInsert(List<Component> components, String key, String additional, ChatFormatting... formatings) {
        transInsert(components::add, key, additional, formatings);
    }

    public static void transtrans(List<Component> components, String key, String additional, ChatFormatting... formatings) {
        transtrans(components::add, key, additional, formatings);
    }

    public static void tt(List<Component> components, MutableComponent c, ChatFormatting... formatings) {
        tt(components::add, c, formatings);
    }

    public static MutableComponent apply(MutableComponent component, ChatFormatting... formattings) {
        for (ChatFormatting formatting : formattings) {
            component = component.withStyle(formatting);
        }
        return component;
    }
}
