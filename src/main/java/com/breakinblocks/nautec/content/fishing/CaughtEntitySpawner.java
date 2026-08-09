package com.breakinblocks.nautec.content.fishing;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.List;

public final class CaughtEntitySpawner {
    public static void releaseAll(FishingHook hook, List<ItemStack> items) {
        if (!(hook.level() instanceof ServerLevel level)) {
            return;
        }

        Iterator<ItemStack> iterator = items.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            EntityType<?> type = stack.get(NTDataComponents.CATCH_ENTITY.get());
            if (type == null) {
                continue;
            }
            iterator.remove();
            for (int i = 0; i < stack.getCount(); i++) {
                spawn(level, hook, type);
            }
        }
    }

    private static void spawn(ServerLevel level, FishingHook hook, EntityType<?> type) {
        Entity entity = type.create(level, EntitySpawnReason.NATURAL);
        if (entity == null) {
            Nautec.LOGGER.warn("A lucky fishing zone tried to catch {} but it could not be created", type);
            return;
        }

        entity.snapTo(hook.getX(), hook.getY(), hook.getZ(), level.getRandom().nextFloat() * 360.0F, 0.0F);

        Entity owner = hook.getOwner();
        if (owner != null) {
            Vec3 toOwner = owner.position().subtract(hook.position());
            entity.setDeltaMovement(toOwner.x * 0.08, toOwner.y * 0.08 + Math.sqrt(toOwner.length()) * 0.16, toOwner.z * 0.08);
        }

        level.addFreshEntity(entity);
    }

    private CaughtEntitySpawner() {
    }
}
