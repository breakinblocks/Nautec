package com.breakinblocks.nautec.api.gateways;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public record GatewayAddress(List<DyeColor> slots) {
    public static final int SLOTS = 4;

    public static final List<DyeColor> PALETTE = List.of(
            DyeColor.WHITE,
            DyeColor.LIGHT_BLUE,
            DyeColor.CYAN,
            DyeColor.BLUE,
            DyeColor.PURPLE,
            DyeColor.MAGENTA,
            DyeColor.LIME,
            DyeColor.BLACK
    );

    public static final GatewayAddress DEFAULT = uniform(DyeColor.CYAN);

    public static final Codec<GatewayAddress> CODEC = Codec.INT.xmap(GatewayAddress::unpack, GatewayAddress::pack);

    public static final StreamCodec<io.netty.buffer.ByteBuf, GatewayAddress> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(GatewayAddress::unpack, GatewayAddress::pack);

    public GatewayAddress {
        if (slots.size() != SLOTS) {
            throw new IllegalArgumentException("A gateway address needs exactly " + SLOTS + " slots, got " + slots.size());
        }
        slots = List.copyOf(slots);
    }

    public static GatewayAddress uniform(DyeColor colour) {
        return new GatewayAddress(List.of(colour, colour, colour, colour));
    }

    public int pack() {
        int packed = 0;
        for (DyeColor colour : slots) {
            packed = packed * PALETTE.size() + PALETTE.indexOf(colour);
        }
        return packed;
    }

    public static GatewayAddress unpack(int packed) {
        int value = Math.floorMod(packed, addressCount());
        DyeColor[] colours = new DyeColor[SLOTS];
        for (int i = SLOTS - 1; i >= 0; i--) {
            colours[i] = PALETTE.get(value % PALETTE.size());
            value /= PALETTE.size();
        }
        return new GatewayAddress(List.of(colours));
    }

    public static int addressCount() {
        int count = 1;
        for (int i = 0; i < SLOTS; i++) {
            count *= PALETTE.size();
        }
        return count;
    }

    public GatewayAddress withSlot(int index, DyeColor colour) {
        if (index < 0 || index >= SLOTS) {
            return this;
        }
        DyeColor[] colours = slots.toArray(new DyeColor[0]);
        colours[index] = colour;
        return new GatewayAddress(List.of(colours));
    }

    public static boolean isUsable(DyeColor colour) {
        return PALETTE.contains(colour);
    }

    public static DyeColor colourOf(Item item) {
        for (DyeColor colour : PALETTE) {
            if (dyeItem(colour) == item) {
                return colour;
            }
        }
        return null;
    }

    public static Item dyeItem(DyeColor colour) {
        return switch (colour) {
            case WHITE -> Items.WHITE_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case CYAN -> Items.CYAN_DYE;
            case BLUE -> Items.BLUE_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIME -> Items.LIME_DYE;
            case BLACK -> Items.BLACK_DYE;
            default -> Items.AIR;
        };
    }

    public Component describe() {
        MutableComponent text = Component.empty();
        for (int i = 0; i < slots.size(); i++) {
            if (i > 0) {
                text.append(Component.literal(" ").withStyle(ChatFormatting.GRAY));
            }
            DyeColor colour = slots.get(i);
            text.append(Component.translatable("color.minecraft." + colour.getName()).withStyle(style -> style.withColor(colour.getTextColor())));
        }
        return text;
    }
}
