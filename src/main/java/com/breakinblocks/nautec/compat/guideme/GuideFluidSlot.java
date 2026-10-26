package com.breakinblocks.nautec.compat.guideme;

import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.document.interaction.TextTooltip;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

final class GuideFluidSlot extends LytBlock implements InteractiveElement {
    private final List<FluidStack> fluids;

    GuideFluidSlot(List<FluidStack> fluids) {
        this.fluids = fluids.stream().map(FluidStack::copy).toList();
    }

    private FluidStack displayed() {
        long cycle = System.nanoTime() / TimeUnit.SECONDS.toNanos(2);
        return fluids.get((int) (cycle % fluids.size()));
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, 18, 18);
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
    }

    @Override
    public void render(RenderContext context) {
        context.fillIcon(bounds, GuiAssets.SLOT);
        context.renderFluid(displayed(), bounds.x() + 1, bounds.y() + 1, 0, 16, 16);
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        FluidStack fluid = displayed();
        return Optional.of(new TextTooltip(fluid.getHoverName(), Component.translatable("nautec.guide.recipe.fluid_amount", fluid.getAmount())));
    }
}
