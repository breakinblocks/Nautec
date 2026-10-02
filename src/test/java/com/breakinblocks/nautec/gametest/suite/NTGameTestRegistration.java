package com.breakinblocks.nautec.gametest.suite;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class NTGameTestRegistration {
    private NTGameTestRegistration() {
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        NTTestRegistrar r = new NTTestRegistrar(event);

        r.add("framework/arena_smoke", 20, helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, Blocks.STONE.defaultBlockState());
            helper.succeedWhenBlockPresent(Blocks.STONE, pos);
        });

        PowerAndLaserTests.register(r);
        MachineTests.register(r);
        MultiblockTests.register(r);
        CrateAndCapabilityTests.register(r);
        RecipeAndBacteriaTests.register(r);
        BacteriaMachineTests.register(r);
        GeneratedPackTests.register(r);
        PersistenceTests.register(r);
        WorldgenInjectionTests.register(r);
        ContentIntegrityTests.register(r);
        AquaticMovementTests.register(r);
        LootTableTests.register(r);
        LuckyZoneTests.register(r);
        FishingHookFlowTests.register(r);
        WaveJetTests.register(r);
        AtlanteanRifleTests.register(r);
        NeptunesTridentTests.register(r);
        SubmarineTests.register(r);
        GatewayTests.register(r);
        ReviewRegressionTests.register(r);
        ReleaseRegressionTests.register(r);
        ClientAcceptanceRegressionTests.register(r);
        LaserFixTests.register(r);
        BiologyFixTests.register(r);
        DataFixTests.register(r);
        MachineFixTests.register(r);
        NavalFishingFixTests.register(r);
        ShowcaseTests.register(r);
        BioReactorOverhaulTests.register(r);
        CatalystFeedbackTests.register(r);
        EyeOfTheSeaTests.register(r);
        ConfinedSpawnerTests.register(r);
        DrainTests.register(r);
        if (r.registeredCount() != 448) {
            throw new IllegalStateException("Expected 448 Nautec suite tests, registered " + r.registeredCount());
        }
    }
}
