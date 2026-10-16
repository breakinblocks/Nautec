package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.bacteria.BacteriaLineage;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class StrainLineageEmiRecipe extends NTEmiRecipe<BacteriaLineage.Path> {
    private static final ResourceLocation ARROW_SPRITE = Nautec.rl("container/bio_reactor/progress_arrow_off");
    public static final int MAX_ROWS = 8;
    private static final int HEADER = 12;
    private static final int ROW = 20;
    private static final int CATALYST_X = 22;
    private static final int ARROW_X = 44;
    private static final int CHANCE_X = 72;
    private static final int OUTPUT_X = 102;
    private static final int WIDTH = OUTPUT_X + 18;

    public StrainLineageEmiRecipe(BacteriaLineage.Path path) {
        super(NTEmiPlugin.STRAIN_LINEAGE, id(path), path, WIDTH, HEADER + ROW * Math.max(1, rows(path).size()));
        outputs.add(NTEmiStacks.dish(path.target()));
        fit(0, header(path));
    }

    private static Component header(BacteriaLineage.Path path) {
        return Component.translatable("nautec.jei.lineage.header", path.steps().size());
    }

    private static ResourceLocation id(BacteriaLineage.Path path) {
        ResourceLocation target = path.target().location();
        return ResourceLocation.fromNamespaceAndPath("nautec", "/strain_lineage/" + target.getNamespace() + "/" + target.getPath());
    }

    private static List<BacteriaMutationRecipe> rows(BacteriaLineage.Path path) {
        List<BacteriaMutationRecipe> steps = path.steps();
        return steps.size() > MAX_ROWS ? steps.subList(steps.size() - MAX_ROWS, steps.size()) : steps;
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int line = EmiCanvas.lineHeight();
        canvas.text(header(recipe), 0, 0, layoutWidth(), EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        List<BacteriaMutationRecipe> rows = rows(recipe);
        for (int i = 0; i < rows.size(); i++) {
            BacteriaMutationRecipe step = rows.get(i);
            int y = HEADER + i * ROW;
            canvas.sprite(ARROW_SPRITE, ARROW_X, y + 4, 24, 10);
            canvas.bacteria(step.inputBacteria(), 0, y);
            canvas.bacteria(step.resultBacteria(), OUTPUT_X, y);
            canvas.slot(EmiIngredient.of(step.catalyst()), CATALYST_X + 1, y + 1);
            canvas.text(Component.literal(Math.round(step.chance()) + "%"), CHANCE_X, y + 5, OUTPUT_X - CHANCE_X, line,
                    EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        }
    }
}
