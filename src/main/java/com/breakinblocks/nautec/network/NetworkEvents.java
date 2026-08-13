package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Nautec.MODID)
public class NetworkEvents {
    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Nautec.MODID);
        registrar.playToServer(
                KeyPressedPayload.TYPE,
                KeyPressedPayload.STREAM_CODEC,
                KeyPressedPayload::keyPressedAction
        );
        registrar.playToServer(
                BacteriaSlotClickedPayload.TYPE,
                BacteriaSlotClickedPayload.STREAM_CODEC,
                BacteriaSlotClickedPayload::handle
        );
        registrar.playBidirectional(
                SyncAugmentPayload.TYPE,
                SyncAugmentPayload.STREAM_CODEC,
                SyncAugmentPayload::setAugmentDataAction,
                SyncAugmentPayload::setAugmentDataAction
        );
        registrar.playBidirectional(
                SetCooldownPayload.TYPE,
                SetCooldownPayload.STREAM_CODEC,
                SetCooldownPayload::setCooldownAction,
                SetCooldownPayload::setCooldownAction
        );
        registrar.playBidirectional(
                StartAugmentationPayload.TYPE,
                StartAugmentationPayload.STREAM_CODEC,
                StartAugmentationPayload::startAugmentation,
                StartAugmentationPayload::startAugmentation
        );
        registrar.playToClient(
                OpenFishingMinigamePayload.TYPE,
                OpenFishingMinigamePayload.STREAM_CODEC,
                OpenFishingMinigamePayload::handle
        );
        registrar.playToServer(
                FishingMinigameResultPayload.TYPE,
                FishingMinigameResultPayload.STREAM_CODEC,
                FishingMinigameResultPayload::handle
        );
        registrar.playToClient(
                OpenGatewayScreenPayload.TYPE,
                OpenGatewayScreenPayload.STREAM_CODEC,
                OpenGatewayScreenPayload::handle
        );
        registrar.playToServer(
                SetGatewayAddressPayload.TYPE,
                SetGatewayAddressPayload.STREAM_CODEC,
                SetGatewayAddressPayload::handle
        );
        registrar.playToServer(
                SubmarineAbilityPayload.TYPE,
                SubmarineAbilityPayload.STREAM_CODEC,
                SubmarineAbilityPayload::handle
        );
        registrar.playToClient(
                SubmarineCooldownPayload.TYPE,
                SubmarineCooldownPayload.STREAM_CODEC,
                SubmarineCooldownPayload::handle
        );
        registrar.playToClient(
                SonarPingPayload.TYPE,
                SonarPingPayload.STREAM_CODEC,
                SonarPingPayload::handle
        );
        registrar.playToClient(
                TeleportFxPayload.TYPE,
                TeleportFxPayload.STREAM_CODEC,
                TeleportFxPayload::handle
        );
        registrar.playBidirectional(
                ClearAugmentPayload.TYPE,
                ClearAugmentPayload.STREAM_CODEC,
                ClearAugmentPayload::clearAugmentAction,
                ClearAugmentPayload::clearAugmentAction
        );
    }
}
