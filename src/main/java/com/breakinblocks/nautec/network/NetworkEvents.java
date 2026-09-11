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
        final PayloadRegistrar registrar = event.registrar("2");
        registrar.playToClient(
                OpenAugmentationScreenPayload.TYPE,
                OpenAugmentationScreenPayload.STREAM_CODEC,
                OpenAugmentationScreenPayload::handle
        );
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
        registrar.playToClient(
                SyncAugmentPayload.TYPE,
                SyncAugmentPayload.STREAM_CODEC,
                SyncAugmentPayload::setAugmentDataAction
        );
        registrar.playToClient(
                SetCooldownPayload.TYPE,
                SetCooldownPayload.STREAM_CODEC,
                SetCooldownPayload::setCooldownAction
        );
        registrar.playToServer(
                StartAugmentationPayload.TYPE,
                StartAugmentationPayload.STREAM_CODEC,
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
        registrar.playToServer(
                ToggleWaveJetLightPayload.TYPE,
                ToggleWaveJetLightPayload.STREAM_CODEC,
                ToggleWaveJetLightPayload::handle
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
        registrar.playToClient(
                ClearAugmentPayload.TYPE,
                ClearAugmentPayload.STREAM_CODEC,
                ClearAugmentPayload::clearAugmentAction
        );
    }
}
