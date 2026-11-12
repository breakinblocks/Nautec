package com.breakinblocks.nautec.content.enrichment;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.CrystalPurity;

public final class EnrichmentRules {
    private static final float REFERENCE_PURITY = CrystalPurity.CRADLE_MAX;

    private EnrichmentRules() {
    }

    public static float maxPurity() {
        return CrystalPurity.clamp((float) NTConfig.enrichmentMaxPurity);
    }

    public static float minPurity() {
        return CrystalPurity.clamp((float) NTConfig.enrichmentMinCrystalPurity);
    }

    public static boolean enrichable(float purity) {
        return purity >= minPurity() - 0.0001f;
    }

    public static float overreach(float purity, float beamPurity) {
        return purity - beamPurity;
    }

    public static boolean overreached(float purity, float beamPurity) {
        return overreach(purity, beamPurity) >= NTConfig.enrichmentMaxOverreach - 0.0001;
    }

    public static long cycleAp(float purity) {
        double cost = NTConfig.enrichmentBaseAp * Math.pow(NTConfig.enrichmentApGrowth, purity - REFERENCE_PURITY);
        return Math.max(1L, Math.round(cost));
    }

    public static float resistanceRatio(BacteriaInstance colony) {
        if (colony.isEmpty()) {
            return 0f;
        }
        double cap = NTConfig.bacteriaMutationResistanceCap;
        return cap <= 0 ? 1f : (float) Math.clamp(colony.getStats().mutationResistance() / cap, 0.0, 1.0);
    }

    public static long colonyCost(float purity, float beamPurity, float resistanceRatio) {
        double cost = NTConfig.enrichmentColonyBase * Math.pow(NTConfig.enrichmentColonyGrowth, purity - REFERENCE_PURITY);
        cost *= 1 + NTConfig.enrichmentOverreachColony * Math.max(0f, overreach(purity, beamPurity));
        cost *= 1 - NTConfig.enrichmentResistanceSaving * Math.clamp(resistanceRatio, 0f, 1f);
        return Math.max(0L, Math.round(cost));
    }

    public static float regressionChance(float purity, float beamPurity) {
        double overreach = overreach(purity, beamPurity);
        double parity = NTConfig.enrichmentParityChance;
        double chance;
        if (overreach <= 0) {
            double margin = NTConfig.enrichmentSafeMargin;
            chance = NTConfig.enrichmentMinChance + (parity - NTConfig.enrichmentMinChance) * Math.clamp((overreach + margin) / margin, 0.0, 1.0);
        } else {
            chance = parity + (NTConfig.enrichmentMaxChance - parity) * Math.min(1.0, overreach / NTConfig.enrichmentMaxOverreach);
        }
        return (float) Math.clamp(chance, 0.0, 1.0);
    }

    public static float success(float purity, float beamPurity, float target) {
        float raised = purity + (float) NTConfig.enrichmentStep;
        raised = Math.min(raised, beamPurity + (float) NTConfig.enrichmentMaxOverreach);
        raised = Math.min(raised, Math.min(target, maxPurity()));
        return CrystalPurity.snap(Math.max(purity, raised));
    }

    public static float regress(float purity) {
        float floor = Math.min(purity, minPurity());
        return CrystalPurity.snap(Math.max(purity - (float) NTConfig.enrichmentRegression, floor));
    }
}
