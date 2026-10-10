package com.breakinblocks.nautec.compat.stellaris;


import com.breakinblocks.nautec.network.AirlessUntilPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.DivingSuitArmorItem;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Nautec.MODID)
public final class StellarisCompat {
    public static final String MOD_ID = "stellaris";
    public static final ResourceKey<DamageType> OXYGEN_DEPRIVATION =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "oxygen_deprivation"));
    private static final int DEFAULT_DAMAGE_INTERVAL_TICKS = 20;
    private static final int AIRLESS_GRACE_TICKS = 10;

    private StellarisCompat() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!NTConfig.divingSuitProtectsInSpace
                || !event.getSource().is(OXYGEN_DEPRIVATION)
                || !(event.getEntity() instanceof Player player)
                || !DivingSuitArmorItem.isWearingFullSuit(player)) {
            return;
        }

        int interval = damageIntervalTicks();
        long airlessUntil = player.level().getGameTime() + interval + AIRLESS_GRACE_TICKS;
        player.setData(NTDataAttachments.AIRLESS_UNTIL, airlessUntil);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new AirlessUntilPayload(airlessUntil));
        }

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int oxygen = NTDataComponentsUtils.getOxygenLevels(chestplate);
        if (oxygen <= 0) {
            return;
        }

        int drain = Math.max(1, Math.ceilDiv(interval, 20));
        NTDataComponentsUtils.setOxygenLevels(chestplate, Math.max(0, oxygen - drain));
        event.setCanceled(true);
    }

    private static int damageIntervalTicks() {
        try {
            Object config = Class.forName("org.exodusstudio.stellaris.Stellaris").getField("CONFIG").get(null);
            Object oxygenConfig = config.getClass().getField("oxygenConfig").get(config);
            return oxygenConfig.getClass().getField("oxygenDamageInterval").getInt(oxygenConfig);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return DEFAULT_DAMAGE_INTERVAL_TICKS;
        }
    }
}
