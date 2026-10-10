package com.breakinblocks.nautec.utils;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public final class FluidInteractions {
    private FluidInteractions() {
    }

    public static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    public static boolean interact(Player player, InteractionHand hand, @Nullable ResourceHandler<FluidResource> handler) {
        IFluidHandler exposed = TransferCapabilities.exposeFluids(handler);
        return exposed != null && FluidUtil.interactWithFluidHandler(player, hand, exposed);
    }

    public static boolean interact(Player player, InteractionHand hand, @Nullable IFluidHandler handler) {
        return handler != null && FluidUtil.interactWithFluidHandler(player, hand, handler);
    }
}
