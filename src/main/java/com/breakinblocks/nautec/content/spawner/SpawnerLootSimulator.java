package com.breakinblocks.nautec.content.spawner;

import java.util.function.IntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class SpawnerLootSimulator {
    private final Map<SpawnData, Optional<LivingEntity>> mobs = new IdentityHashMap<>();

    public void clear() {
        mobs.clear();
    }

    public void roll(ServerLevel level, BlockPos pos, SpawnData data, int times, Consumer<ItemStack> output, IntConsumer experience) {
        if (times <= 0) {
            return;
        }
        Optional<LivingEntity> mob = mobs.computeIfAbsent(data, key -> Optional.ofNullable(create(level, pos, key)));
        if (mob.isEmpty()) {
            return;
        }
        LivingEntity living = mob.get();
        FakePlayer killer = FakePlayerFactory.getMinecraft(level);
        int xp = 0;
        for (int i = 0; i < times; i++) {
            xp += living.getExperienceReward(level, killer);
        }
        if (xp > 0) {
            experience.accept(xp);
        }
        ResourceKey<LootTable> key = living.getLootTable();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        if (table == LootTable.EMPTY) {
            return;
        }
        DamageSource source = level.damageSources().playerAttack(killer);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, living)
                .withParameter(LootContextParams.ORIGIN, living.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withParameter(LootContextParams.ATTACKING_ENTITY, killer)
                .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, killer)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer)
                .create(LootContextParamSets.ENTITY);
        for (int i = 0; i < times; i++) {
            table.getRandomItems(params, output);
        }
    }

    private static @Nullable LivingEntity create(ServerLevel level, BlockPos pos, SpawnData data) {
        if (!data.getEntityToSpawn().contains("id", Tag.TAG_STRING)) {
            return null;
        }
        Entity entity = EntityType.loadEntityRecursive(data.getEntityToSpawn(), level, loaded -> {
            loaded.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
            return loaded;
        });
        return entity instanceof LivingEntity living ? living : null;
    }
}
