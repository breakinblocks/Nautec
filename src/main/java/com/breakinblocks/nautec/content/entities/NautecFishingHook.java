package com.breakinblocks.nautec.content.entities;

import com.breakinblocks.nautec.content.fishing.CaughtEntitySpawner;
import com.breakinblocks.nautec.content.fishing.FishingMinigame;
import com.breakinblocks.nautec.content.fishing.MinigameKind;
import com.breakinblocks.nautec.content.items.tools.NautecFishingRodItem;
import com.breakinblocks.nautec.mixin.FishingHookAccessor;
import com.breakinblocks.nautec.network.OpenFishingMinigamePayload;
import com.breakinblocks.nautec.registries.NTEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
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

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

public class NautecFishingHook extends FishingHook {
    public static final int REEL_WINDOW_TICKS = 40;
    public static final int WIN_SCREEN_TICKS = 10;
    public static final int REPORT_GRACE_TICKS = 20;
    public static final int EARLY_REPORT_TOLERANCE_TICKS = 10;

    private int holdBiteUntil = -1;
    private boolean biteAnnounced;
    private MinigameKind kind = MinigameKind.TIMING_BAR;
    private long nonce;
    private long seed;
    private int challengeStartedAt = -1;
    private boolean minigameSucceeded;
    private int autoReelAt = -1;

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

        Vec3 movement = new Vec3(
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

        if (this.level().isClientSide() || this.isRemoved() || !(this.getPlayerOwner() instanceof Player player)) {
            return;
        }

        if (this.autoReelAt >= 0 && this.tickCount >= this.autoReelAt) {
            this.autoReelAt = -1;
            autoReel(player);
            return;
        }

        FishingHookAccessor accessor = (FishingHookAccessor) this;
        int nibble = accessor.nautec$getNibble();
        if (nibble <= 0) {
            if (this.biteAnnounced) {
                resetChallenge();
            }
            return;
        }

        if (!this.biteAnnounced) {
            this.biteAnnounced = true;
            this.kind = MinigameKind.random(this.random);
            this.nonce = this.random.nextLong();
            this.seed = this.random.nextLong();
            this.challengeStartedAt = this.tickCount;
            this.holdBiteUntil = this.tickCount + FishingMinigame.DURATION_TICKS + REEL_WINDOW_TICKS;
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new OpenFishingMinigamePayload(this.kind.ordinal(), this.nonce, this.seed));
            }
        }

        if (nibble < 2 && this.tickCount < this.holdBiteUntil) {
            accessor.nautec$setNibble(2);
        }
    }

    private void resetChallenge() {
        this.biteAnnounced = false;
        this.challengeStartedAt = -1;
        this.holdBiteUntil = -1;
        this.minigameSucceeded = false;
        this.autoReelAt = -1;
    }

    private void autoReel(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).getItem() instanceof NautecFishingRodItem && player.fishing == this) {
                NautecFishingRodItem.reel(this.level(), player, hand);
                player.swing(hand, true);
                return;
            }
        }
    }

    public MinigameKind minigameKind() {
        return this.kind;
    }

    public long minigameNonce() {
        return this.nonce;
    }

    public long minigameSeed() {
        return this.seed;
    }

    public boolean minigameSucceeded() {
        return this.minigameSucceeded;
    }

    public void onMinigameReport(long reportedNonce, List<Integer> reportedTicks) {
        if (this.challengeStartedAt < 0 || reportedNonce != this.nonce || this.minigameSucceeded) {
            return;
        }

        int elapsed = this.tickCount - this.challengeStartedAt;
        if (elapsed > FishingMinigame.DURATION_TICKS + REPORT_GRACE_TICKS) {
            return;
        }
        for (int tick : reportedTicks) {
            if (tick > elapsed + EARLY_REPORT_TOLERANCE_TICKS) {
                return;
            }
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
        this.autoReelAt = this.tickCount + WIN_SCREEN_TICKS;
        this.holdBiteUntil = Math.max(this.holdBiteUntil, this.tickCount + WIN_SCREEN_TICKS + REEL_WINDOW_TICKS);
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
            deliverCatch(level, owner, this, table.getRandomItems(params), x, y, z);
        }

        if (this.minigameSucceeded) {
            level.addFreshEntity(new ExperienceOrb(level, x, y + 0.5, z, 4 + this.random.nextInt(5)));
        }
    }

    public static void deliverCatch(ServerLevel level, Player owner, FishingHook hook, List<ItemStack> stacks, double x, double y, double z) {
        List<ItemStack> items = new ArrayList<>(stacks);
        CaughtEntitySpawner.releaseAll(hook, items);
        for (ItemStack stack : items) {
            dropTowards(level, owner, stack, x, y, z);
        }
    }

    public static void dropTowards(ServerLevel level, Player owner, ItemStack stack, double x, double y, double z) {
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        double dx = owner.getX() - x;
        double dy = owner.getY() - y;
        double dz = owner.getZ() - z;
        item.setDeltaMovement(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
        level.addFreshEntity(item);
    }
}
