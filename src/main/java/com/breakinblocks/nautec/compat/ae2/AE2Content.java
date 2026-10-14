package com.breakinblocks.nautec.compat.ae2;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.items.parts.PartItem;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.content.distributor.NetworkStorages;
import com.breakinblocks.nautec.registries.NTCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AE2Content {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Nautec.MODID);

    public static final DeferredItem<PartItem<LaserP2PTunnelPart>> LASER_P2P_TUNNEL = ITEMS.registerItem("laser_p2p_tunnel",
            properties -> new PartItem<>(properties, LaserP2PTunnelPart.class, LaserP2PTunnelPart::new));

    private AE2Content() {
    }

    static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(AE2Content::registerPartCapabilities);
        modEventBus.addListener(AE2Content::commonSetup);
        modEventBus.addListener(AE2Content::creativeTabs);
        NetworkStorages.register(MENetworkStorage::find);
    }

    private static void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        event.register(NTCapabilities.BeamReceiver.BLOCK, (part, side) -> part.receivesBeams() ? part : null, LaserP2PTunnelPart.class);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> P2PTunnelAttunement.registerAttunementTag(LASER_P2P_TUNNEL.get()));
    }

    private static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == NTCreativeTabs.MAIN.get()) {
            event.accept(LASER_P2P_TUNNEL.get());
        }
    }
}
