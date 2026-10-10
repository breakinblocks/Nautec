package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.datagen.loot.BlockLootTableProvider;
import com.breakinblocks.nautec.datagen.loot.ChestLootTableProvider;
import com.breakinblocks.nautec.datagen.loot.EntityLootTableProvider;
import com.breakinblocks.nautec.datagen.loot.FishingLootTableProvider;
import com.breakinblocks.nautec.datagen.loot.LootModifierProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Nautec.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGatherer {
    private static final String PATH_PREFIX = "textures/block";
    private static final String PATH_SUFFIX = ".png";

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        boolean client = event.includeClient();
        boolean server = event.includeServer();

        generator.addProvider(client, new BlockModelProvider(output, existingFileHelper));
        generator.addProvider(client, new NTItemModelProvider(output, existingFileHelper));
        generator.addProvider(server, new RecipesProvider(output, lookupProvider));
        generator.addProvider(server, new LootTableProvider(output, Collections.emptySet(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLootTableProvider::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(provider -> new ChestLootTableProvider(), LootContextParamSets.CHEST),
                new LootTableProvider.SubProviderEntry(EntityLootTableProvider::new, LootContextParamSets.ENTITY),
                new LootTableProvider.SubProviderEntry(FishingLootTableProvider::new, LootContextParamSets.FISHING)
        ), lookupProvider));
        generator.addProvider(server, new BlockTagProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(server, new ItemTagProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(server, new EntityTypeTagProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(server, new LootModifierProvider(output, lookupProvider));

        DatapackRegistryProvider datapackRegistries = generator.addProvider(server, new DatapackRegistryProvider(output, lookupProvider));
        generator.addProvider(server, new BiomeTagProvider(output, datapackRegistries.getRegistryProvider(), existingFileHelper));
        generator.addProvider(server, new DamageTypeTagProvider(output, datapackRegistries.getRegistryProvider(), existingFileHelper));
        generator.addProvider(server, new AdvancementProvider(output, datapackRegistries.getRegistryProvider(), List.of(new NTAdvancements())));
        generator.addProvider(server, new NTDataMapProvider(output, lookupProvider));
        generator.addProvider(server, new BacteriaMaterialProvider(output, lookupProvider));
        generator.addProvider(server, new LithostitchedInjectorProvider(output));
        generator.addProvider(client, new EnUsProvider(output));
    }
}
