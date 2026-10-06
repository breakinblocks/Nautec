package com.breakinblocks.nautec.compat.starcatcher;

import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import com.wdiscute.starcatcher.data.FishCaughtCounter;
import com.wdiscute.starcatcher.data.attachments.FishingGuideAttachment;
import com.wdiscute.starcatcher.fish.FishApi;
import com.wdiscute.starcatcher.fish.FishProperties;
import com.wdiscute.starcatcher.registry.SCCriterionTriggers;
import com.wdiscute.starcatcher.registry.SCDataAttachments;
import com.wdiscute.starcatcher.registry.fishrestrictions.AbstractFishRestriction;
import com.wdiscute.starcatcher.tournament.TournamentHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

final class StarcatcherCatches {
    private StarcatcherCatches() {
    }

    static boolean isReeling(Player player) {
        return !SCDataAttachments.get(player, SCDataAttachments.FISHING_BOB).isEmpty();
    }

    static @Nullable List<ItemStack> roll(NautecFishingHook hook, LootParams params) {
        if (!(hook.getPlayerOwner() instanceof ServerPlayer player)) {
            return null;
        }
        ServerLevel level = params.getLevel();
        ItemStack rod = params.contextMap().getOptional(LootContextParams.TOOL) instanceof ItemStack stack ? stack : ItemStack.EMPTY;

        FishProperties fp = pick(hook, level, rod, hook.getRandom());
        if (fp == null) {
            return null;
        }

        RandomSource random = hook.getRandom();
        boolean perfect = hook.minigameSucceeded();
        float percentile = random.nextFloat() * 100.0F;
        boolean golden = perfect
                && FishCaughtCounter.canCatchGolden(fp, player)
                && random.nextFloat() < fp.sizeWeight().goldenChance();
        Identifier id = FishApi.getKey(level, fp);
        boolean firstCatch = id != null && !FishingGuideAttachment.getFishesCaught(player).containsKey(id);

        if (id != null) {
            SCCriterionTriggers.FISH.get().trigger(player, id, fp.rarity(), 0, perfect);
        }
        FishCaughtCounter.awardFishCaughtCounter(fp, id, player, 0, percentile, perfect, true, golden, firstCatch);
        TournamentHandler.addScore(player, fp, perfect, percentile);
        player.giveExperiencePoints(fp.rarity().getXp());

        List<ItemStack> caught = new ArrayList<>();
        caught.add(FishApi.makeItemStackNonBucket(fp, percentile, golden, player, perfect));
        return caught;
    }

    private static @Nullable FishProperties pick(NautecFishingHook hook, ServerLevel level, ItemStack rod, RandomSource random) {
        List<FishProperties> candidates = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int total = 0;
        for (FishProperties fp : FishApi.getFishes(level)) {
            int chance = fp.calculateChance(hook, level, rod, AbstractFishRestriction.Context.FISHING);
            if (chance > 0) {
                candidates.add(fp);
                weights.add(chance);
                total += chance;
            }
        }
        if (total <= 0) {
            return null;
        }
        int roll = random.nextInt(total);
        for (int i = 0; i < candidates.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return candidates.get(i);
            }
        }
        return candidates.getLast();
    }
}
