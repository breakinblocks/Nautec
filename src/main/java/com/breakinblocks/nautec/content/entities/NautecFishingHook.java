package com.breakinblocks.nautec.content.entities;

import com.breakinblocks.nautec.content.fishing.FishingMinigame;
import com.breakinblocks.nautec.content.fishing.MinigameKind;
import com.breakinblocks.nautec.mixin.FishingHookAccessor;
import com.breakinblocks.nautec.network.OpenFishingMinigamePayload;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NautecFishingHook extends FishingHook {
    private boolean biteAnnounced;
    private MinigameKind kind = MinigameKind.TIMING_BAR;
    private long nonce;
    private long seed;
    private int challengeStartedAt = -1;
    private boolean minigameSucceeded;

    public NautecFishingHook(EntityType<? extends NautecFishingHook> type, Level level) {
        super(type, level);
    }

    public NautecFishingHook(Player player, Level level, int luck, int lureSpeed) {
        super(NTEntities.NAUTEC_FISHING_HOOK.get(), level);
        FishingHookAccessor accessor = (FishingHookAccessor) this;
        accessor.nautec$setLuck(luck);
        accessor.nautec$setLureSpeed(lureSpeed);
        this.setOwner(player);
        aimFrom(player);
    }

    private void aimFrom(Player player) {
        float xRot = player.getXRot();
        float yRot = player.getYRot();
        float yCos = Mth.cos(-yRot * (float) (Math.PI / 180.0) - (float) Math.PI);
        float ySin = Mth.sin(-yRot * (float) (Math.PI / 180.0) - (float) Math.PI);
        float xCos = -Mth.cos(-xRot * (float) (Math.PI / 180.0));
        float xSin = Mth.sin(-xRot * (float) (Math.PI / 180.0));

        this.snapTo(player.getX() - ySin * 0.3, player.getEyeY(), player.getZ() - yCos * 0.3, yRot, xRot);

        net.minecraft.world.phys.Vec3 movement = new net.minecraft.world.phys.Vec3(
                -ySin, Mth.clamp(-(xSin / xCos), -5.0F, 5.0F), -yCos);
        double length = movement.length();
        movement = movement.multiply(
                0.6 / length + this.random.triangle(0.5, 0.0103365),
                0.6 / length + this.random.triangle(0.5, 0.0103365),
                0.6 / length + this.random.triangle(0.5, 0.0103365));
        this.setDeltaMovement(movement);
        this.setYRot((float) (Mth.atan2(movement.x, movement.z) * 180.0F / (float) Math.PI));
        this.setXRot((float) (Mth.atan2(movement.y, movement.horizontalDistance()) * 180.0F / (float) Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide() || !(this.getPlayerOwner() instanceof ServerPlayer player)) {
            return;
        }

        int nibble = ((FishingHookAccessor) this).nautec$getNibble();
        if (nibble > 0 && !this.biteAnnounced) {
            this.biteAnnounced = true;
            this.kind = MinigameKind.random(this.random);
            this.nonce = this.random.nextLong();
            this.seed = this.random.nextLong();
            this.challengeStartedAt = this.tickCount;
            PacketDistributor.sendToPlayer(player, new OpenFishingMinigamePayload(this.kind.ordinal(), this.nonce, this.seed));
        }
    }

    public void onMinigameReport(long reportedNonce, List<Integer> reportedTicks) {
        if (this.challengeStartedAt < 0 || reportedNonce != this.nonce || this.minigameSucceeded) {
            return;
        }

        int elapsed = this.tickCount - this.challengeStartedAt;
        if (elapsed > FishingMinigame.DURATION_TICKS + 20) {
            return;
        }

        this.challengeStartedAt = -1;

        int[] report = new int[reportedTicks.size()];
        for (int i = 0; i < report.length; i++) {
            report[i] = reportedTicks.get(i);
        }
        if (!this.kind.validate(this.seed, report)) {
            return;
        }

        this.minigameSucceeded = true;
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.4F);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.2, this.getZ(),
                    10, 0.3, 0.2, 0.3, 0.0);
        }
    }

    @Override
    public int retrieve(@NotNull ItemStack rod) {
        boolean hadBite = ((FishingHookAccessor) this).nautec$getNibble() > 0;
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        Player owner = this.getPlayerOwner();

        int damage = super.retrieve(rod);

        if (hadBite && owner != null && this.level() instanceof ServerLevel level) {
            awardBonus(level, owner, rod, x, y, z);
        }
        return damage;
    }

    private void awardBonus(ServerLevel level, Player owner, ItemStack rod, double x, double y, double z) {
        boolean baseWasTreasure = this.random.nextInt(100) < FishingMinigame.TREASURE_CHANCE;
        List<ResourceKey<LootTable>> tables = FishingMinigame.rewardTables(this.minigameSucceeded, baseWasTreasure);

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.TOOL, rod)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ATTACKING_ENTITY, owner)
                .withLuck(owner.getLuck())
                .create(LootContextParamSets.FISHING);

        for (ResourceKey<LootTable> key : tables) {
            LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
            for (ItemStack stack : table.getRandomItems(params)) {
                dropTowards(level, owner, stack, x, y, z);
            }
        }

        if (this.minigameSucceeded) {
            level.addFreshEntity(new ExperienceOrb(level, x, y + 0.5, z, 4 + this.random.nextInt(5)));
        }
    }

    private void dropTowards(ServerLevel level, Player owner, ItemStack stack, double x, double y, double z) {
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        double dx = owner.getX() - x;
        double dy = owner.getY() - y;
        double dz = owner.getZ() - z;
        item.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
        level.addFreshEntity(item);
    }
}
