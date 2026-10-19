package com.breakinblocks.nautec.compat.emi;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaSelector;
import com.breakinblocks.nautec.api.client.screen.SideConfigHost;
import com.breakinblocks.nautec.api.items.IBacteriaItem;
import com.breakinblocks.nautec.client.screen.ConfinedSpawnerScreen;
import com.breakinblocks.nautec.compat.emi.recipes.AquaticCatalystEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.AugmentationEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.BacteriaGraftingEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.BacteriaIncubationEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.BacteriaMutationEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.BioReactorEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.ColonyFeedingEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.CombustionAdditiveEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.CombustionFuelEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.EasInfusionEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.ItemEtchingEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.ItemTransformationEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.LaserCraftingEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.LaserEnchantingEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.MixingEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.PressureForgingEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.ResonanceCraftingEmiRecipe;
import com.breakinblocks.nautec.compat.emi.recipes.StrainLineageEmiRecipe;
import com.breakinblocks.nautec.content.bacteria.BacteriaLineage;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.blockentities.GraftingStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.enchanter.EnchantingRules;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import com.breakinblocks.nautec.content.menus.RecipeTransfer;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.content.recipes.CombustionAdditiveRecipe;
import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.LaserEnchantingRecipe;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@EmiEntrypoint
public class NTEmiPlugin implements EmiPlugin {
    public static final NTEmiCategory PRESSURE_FORGING = new NTEmiCategory("pressure_forging", "pressure_forging", NTBlocks.PRESSURE_FORGE);
    public static final NTEmiCategory RESONANCE_CRAFTING = new NTEmiCategory("resonance_crafting", "resonance_crafting", NTBlocks.RESONANCE_CHAMBER);
    public static final NTEmiCategory ITEM_TRANSFORMATION = new NTEmiCategory("item_transformation", "item_transformation", NTBlocks.PRISMARINE_RELAY);
    public static final NTEmiCategory AQUATIC_CATALYST_CHANNELING = new NTEmiCategory("aquatic_catalyst_channeling", "aquatic_catalyst_channeling",
            NTBlocks.AQUATIC_CATALYST);
    public static final NTEmiCategory ITEM_ETCHING = new NTEmiCategory("item_etching", "item_etching", NTFluids.ETCHING_ACID.getBucket());
    public static final NTEmiCategory MIXING = new NTEmiCategory(MixingRecipe.NAME, "mixing", NTBlocks.MIXER);
    public static final NTEmiCategory LASER_CRAFTING = new NTEmiCategory(LaserCraftingRecipe.NAME, "laser_crafting", NTBlocks.LASER_CRAFTING_MATRIX);
    public static final NTEmiCategory LASER_ENCHANTING = new NTEmiCategory(LaserEnchantingRecipe.NAME, "laser_enchanting", NTBlocks.ABYSSAL_ENCHANTER);
    public static final NTEmiCategory EAS_INFUSION = new NTEmiCategory("eas_infusion", "eas_infusion", NTFluids.EAS.getBucket());
    public static final NTEmiCategory AUGMENTATION = new NTEmiCategory("augmentation", "augmentation_effects", NTItems.CLAW_ROBOT_ARM);
    public static final NTEmiCategory BACTERIA_MUTATIONS = new NTEmiCategory(BacteriaMutationRecipe.NAME, "bacteria_mutations", NTBlocks.MUTATOR);
    public static final NTEmiCategory STRAIN_LINEAGE = new NTEmiCategory("strain_lineage", "strain_lineage", NTBlocks.MUTATOR);
    public static final NTEmiCategory BACTERIA_INCUBATION = new NTEmiCategory(BacteriaIncubationRecipe.NAME, "bacteria_incubation", NTBlocks.INCUBATOR);
    public static final NTEmiCategory BIO_REACTOR = new NTEmiCategory("bio_reactor", "bio_reactor", NTBlocks.BIO_REACTOR);
    public static final NTEmiCategory COLONY_FEEDING = new NTEmiCategory(ColonyFeedingRecipe.NAME, "colony_feeding", NTBlocks.BIO_REACTOR);
    public static final NTEmiCategory BACTERIA_GRAFTING = new NTEmiCategory("grafting", "bacteria_grafting", NTItems.GRAFTING_TOOL);
    public static final NTEmiCategory COMBUSTION_DYNAMO = new NTEmiCategory("combustion_dynamo", "combustion_dynamo", NTBlocks.COMBUSTION_DYNAMO);
    public static final NTEmiCategory COMBUSTION_ADDITIVE = new NTEmiCategory(CombustionAdditiveRecipe.NAME, "combustion_additive", NTBlocks.COMBUSTION_DYNAMO);

    private static final List<NTEmiCategory> CATEGORIES = List.of(PRESSURE_FORGING, RESONANCE_CRAFTING, ITEM_TRANSFORMATION,
            AQUATIC_CATALYST_CHANNELING, ITEM_ETCHING, MIXING, LASER_CRAFTING, LASER_ENCHANTING, EAS_INFUSION, AUGMENTATION, BACTERIA_MUTATIONS, STRAIN_LINEAGE,
            BACTERIA_INCUBATION, BIO_REACTOR, COLONY_FEEDING, BACTERIA_GRAFTING, COMBUSTION_DYNAMO, COMBUSTION_ADDITIVE);

    @Override
    public void register(EmiRegistry registry) {
        for (NTEmiCategory category : CATEGORIES) {
            registry.addCategory(category);
        }
        registerWorkstations(registry);
        registerComparisons(registry);
        registerRecipes(registry);
        registerInfo(registry);
        registerHandlers(registry);
        registry.addGenericDragDropHandler(new NTDragDropHandler());
        registry.addGenericExclusionArea(NTEmiPlugin::exclusionAreas);
    }

    private static void registerWorkstations(EmiRegistry registry) {
        workstations(registry, PRESSURE_FORGING, NTBlocks.PRESSURE_FORGE);
        workstations(registry, VanillaEmiRecipeCategories.SMELTING, NTBlocks.LASER_PURIFICATION_SMELTER);
        workstations(registry, RESONANCE_CRAFTING, NTBlocks.RESONANCE_CHAMBER);
        workstations(registry, AQUATIC_CATALYST_CHANNELING, NTBlocks.AQUATIC_CATALYST, NTBlocks.ADVANCED_AQUATIC_CATALYST);
        workstations(registry, ITEM_ETCHING, NTFluids.ETCHING_ACID.getBucket());
        workstations(registry, MIXING, NTBlocks.MIXER);
        workstations(registry, LASER_CRAFTING, NTBlocks.LASER_CRAFTING_MATRIX);
        workstations(registry, LASER_ENCHANTING, NTBlocks.ABYSSAL_ENCHANTER);
        workstations(registry, EAS_INFUSION, NTFluids.EAS.getBucket());
        workstations(registry, AUGMENTATION, NTBlocks.AUGMENTATION_STATION);
        workstations(registry, BACTERIA_MUTATIONS, NTBlocks.MUTATOR);
        workstations(registry, STRAIN_LINEAGE, NTBlocks.MUTATOR);
        workstations(registry, BACTERIA_INCUBATION, NTBlocks.INCUBATOR);
        workstations(registry, BIO_REACTOR, NTBlocks.BIO_REACTOR, NTBlocks.INDUSTRIAL_BIO_REACTOR);
        workstations(registry, COLONY_FEEDING, NTBlocks.BIO_REACTOR, NTBlocks.INDUSTRIAL_BIO_REACTOR);
        workstations(registry, COMBUSTION_DYNAMO, NTBlocks.COMBUSTION_DYNAMO);
        workstations(registry, COMBUSTION_ADDITIVE, NTBlocks.COMBUSTION_DYNAMO);
        workstations(registry, BACTERIA_GRAFTING, NTItems.GRAFTING_TOOL, NTItems.PETRI_DISH, NTBlocks.GRAFTING_STATION);
    }

    private static void workstations(EmiRegistry registry, EmiRecipeCategory category, ItemLike... items) {
        for (ItemLike item : items) {
            registry.addWorkstation(category, EmiStack.of(item));
        }
    }

    private static void registerComparisons(EmiRegistry registry) {
        Comparison byStrain = Comparison.compareData(NTEmiStacks::strain);
        for (ItemLike item : NTItems.bacteriaItems()) {
            if (item.asItem() instanceof IBacteriaItem) {
                registry.setDefaultComparison(EmiStack.of(item), byStrain);
            }
        }
    }

    private static void registerRecipes(EmiRegistry registry) {
        RecipeManager manager = registry.getRecipeManager();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RegistryAccess access = level.registryAccess();
        ProductNutrients.Derived derived = ProductNutrients.derive(manager, access);
        SyntheticIds ids = new SyntheticIds();

        for (RecipeHolder<PressureForgingRecipe> holder : manager.getAllRecipesFor(PressureForgingRecipe.Type.INSTANCE)) {
            registry.addRecipe(new PressureForgingEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<ResonanceCraftingRecipe> holder : manager.getAllRecipesFor(ResonanceCraftingRecipe.Type.INSTANCE)) {
            registry.addRecipe(new ResonanceCraftingEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<ItemTransformationRecipe> holder : manager.getAllRecipesFor(ItemTransformationRecipe.Type.INSTANCE)) {
            registry.addRecipe(new ItemTransformationEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<AquaticCatalystChannelingRecipe> holder : manager.getAllRecipesFor(AquaticCatalystChannelingRecipe.Type.INSTANCE)) {
            registry.addRecipe(new AquaticCatalystEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<ItemEtchingRecipe> holder : manager.getAllRecipesFor(ItemEtchingRecipe.Type.INSTANCE)) {
            registry.addRecipe(new ItemEtchingEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<MixingRecipe> holder : manager.getAllRecipesFor(MixingRecipe.Type.INSTANCE)) {
            registry.addRecipe(new MixingEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<LaserCraftingRecipe> holder : manager.getAllRecipesFor(LaserCraftingRecipe.Type.INSTANCE)) {
            registry.addRecipe(new LaserCraftingEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<LaserEnchantingRecipe> holder : manager.getAllRecipesFor(LaserEnchantingRecipe.Type.INSTANCE)) {
            Holder<Enchantment> enchantment = holder.value().enchantment(access).orElse(null);
            if (enchantment != null && EnchantingRules.allowed(enchantment)) {
                registry.addRecipe(new LaserEnchantingEmiRecipe(holder.id(), holder.value(), enchantment));
            }
        }
        for (RecipeHolder<AugmentationRecipe> holder : manager.getAllRecipesFor(AugmentationRecipe.Type.INSTANCE)) {
            registry.addRecipe(new AugmentationEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<CombustionAdditiveRecipe> holder : manager.getAllRecipesFor(CombustionAdditiveRecipe.Type.INSTANCE)) {
            registry.addRecipe(new CombustionAdditiveEmiRecipe(holder.id(), holder.value()));
        }

        List<BacteriaMutationRecipe> mutations = new ArrayList<>();
        for (RecipeHolder<BacteriaMutationRecipe> holder : manager.getAllRecipesFor(BacteriaMutationRecipe.TYPE)) {
            mutations.add(holder.value());
            registry.addRecipe(new BacteriaMutationEmiRecipe(holder.id(), holder.value()));
        }
        for (BacteriaMutationRecipe recipe : derived.mutation()) {
            mutations.add(recipe);
            registry.addRecipe(new BacteriaMutationEmiRecipe(ids.next("mutation/" + path(recipe.inputBacteria()) + "/" + path(recipe.resultBacteria())), recipe));
        }
        for (RecipeHolder<BacteriaIncubationRecipe> holder : manager.getAllRecipesFor(BacteriaIncubationRecipe.TYPE)) {
            registry.addRecipe(new BacteriaIncubationEmiRecipe(holder.id(), holder.value()));
        }
        for (BacteriaIncubationRecipe recipe : derived.incubation()) {
            registry.addRecipe(new BacteriaIncubationEmiRecipe(ids.next("incubation/" + path(recipe.bacteria())), recipe));
        }
        for (RecipeHolder<ColonyFeedingRecipe> holder : manager.getAllRecipesFor(ColonyFeedingRecipe.TYPE)) {
            registry.addRecipe(new ColonyFeedingEmiRecipe(holder.id(), holder.value()));
        }
        for (ColonyFeedingRecipe recipe : derived.feeding()) {
            String target = recipe.bacteria().map(NTEmiPlugin::path).orElse("any");
            registry.addRecipe(new ColonyFeedingEmiRecipe(ids.next("feeding/" + target), recipe));
        }

        for (BacteriaLineage.Path path : BacteriaLineage.fastest(mutations, BacteriaLineage.graftable()).values()) {
            registry.addRecipe(new StrainLineageEmiRecipe(path));
        }

        Registry<Bacteria> bacteria = access.registryOrThrow(NTRegistries.BACTERIA_KEY);
        for (Map.Entry<ResourceKey<Bacteria>, Bacteria> entry : bacteria.entrySet()) {
            if (!entry.getKey().equals(NTBacterias.EMPTY) && !entry.getValue().resource().isEmpty()) {
                registry.addRecipe(new BioReactorEmiRecipe(new BioReactorEmiRecipe.Production(entry.getKey(), entry.getValue().resource())));
            }
        }

        Set<BacteriaGraftingEmiRecipe.Grafting> grafts = new HashSet<>();
        for (Map.Entry<ResourceKey<Block>, BacteriaObtainValue> entry : BuiltInRegistries.BLOCK.getDataMap(NTDataMaps.BACTERIA_OBTAINING).entrySet()) {
            Block block = BuiltInRegistries.BLOCK.getOrThrow(entry.getKey());
            Item sample = block.getCloneItemStack(level, BlockPos.ZERO, block.defaultBlockState()).getItem();
            if (sample == Items.AIR) {
                sample = block.asItem();
            }
            BacteriaGraftingEmiRecipe.Grafting graft = new BacteriaGraftingEmiRecipe.Grafting(sample, entry.getValue());
            if (sample != Items.AIR && grafts.add(graft)) {
                registry.addRecipe(new BacteriaGraftingEmiRecipe(graft));
            }
        }

        for (EasInfusionEmiRecipe recipe : EasInfusionEmiRecipe.all()) {
            registry.addRecipe(recipe);
        }
        registry.addRecipe(new CombustionFuelEmiRecipe());

        for (RecipeHolder<AugmentationRecipe> holder : manager.getAllRecipesFor(AugmentationRecipe.Type.INSTANCE)) {
            AugmentationRecipe recipe = holder.value();
            registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(recipe.augmentItem())), List.of(Component.translatable(recipe.desc())),
                    ids.info("augment/" + holder.id().getNamespace() + "/" + holder.id().getPath())));
        }
    }

    private static void registerInfo(EmiRegistry registry) {
        info(registry, "crystal_seed", List.of(EmiStack.of(NTItems.DORMANT_CRYSTAL_SEED), EmiStack.of(NTItems.PRISMARINE_CRYSTAL_SEED)),
                Component.translatable("nautec.jei.crystal_seed"));
        info(registry, "crystal_cradle", List.of(EmiStack.of(NTBlocks.CRYSTAL_CRADLE)), Component.translatable("nautec.jei.crystal_cradle",
                NumberFormat.getIntegerInstance(Locale.ROOT).format(NTConfig.crystalGrowthPower)));
        info(registry, "eas", List.of(EmiStack.of(NTFluids.EAS.getStillFluid(), 1000), EmiStack.of(NTFluids.EAS.getBucket()),
                EmiStack.of(NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL)), Component.translatable("nautec.jei.info.eas"));
        info(registry, "prismarine_crystal_shard", List.of(EmiStack.of(NTItems.PRISMARINE_CRYSTAL_SHARD)),
                Component.translatable("nautec.jei.info.prismarine_crystal_shard"));
        info(registry, "machine_parts", List.of(EmiStack.of(NTItems.BROKEN_WHISK), EmiStack.of(NTItems.BURNT_COIL), EmiStack.of(NTItems.ANCIENT_VALVE),
                EmiStack.of(NTItems.RUSTY_GEAR)), Component.translatable("nautec.jei.info.machine_parts"));
    }

    private static void info(EmiRegistry registry, String name, List<EmiIngredient> stacks, Component text) {
        registry.addRecipe(new EmiInfoRecipe(stacks, List.of(text), Nautec.rl("/info/" + name)));
    }

    private static void registerHandlers(EmiRegistry registry) {
        registry.addRecipeHandler(NTMenuTypes.MIXER.get(), new MachineRecipeHandler<>(MIXING,
                (menu, recipe) -> counted(((MixingRecipe) recipe).ingredients(), MixerBlockEntity.INPUT_SLOTS)));
        registry.addRecipeHandler(NTMenuTypes.LASER_CRAFTING_MATRIX.get(), new MachineRecipeHandler<>(LASER_CRAFTING,
                (menu, recipe) -> counted(((LaserCraftingRecipe) recipe).ingredients(), LaserCraftingMatrixBlockEntity.INPUT_SLOTS)));
        registry.addRecipeHandler(NTMenuTypes.ABYSSAL_ENCHANTER.get(), new MachineRecipeHandler<>(LASER_ENCHANTING,
                (menu, recipe) -> ((LaserEnchantingEmiRecipe.Selection) recipe).transfer()));
        registry.addRecipeHandler(NTMenuTypes.INCUBATOR.get(), new MachineRecipeHandler<>(BACTERIA_INCUBATION,
                (menu, recipe) -> List.of(new RecipeTransfer.Entry(IncubatorBlockEntity.NUTRIENT_SLOT, ((BacteriaIncubationRecipe) recipe).nutrient(), 1))));
        registry.addRecipeHandler(NTMenuTypes.MUTATOR.get(), new MachineRecipeHandler<>(BACTERIA_MUTATIONS,
                (menu, recipe) -> List.of(new RecipeTransfer.Entry(MutatorBlockEntity.CATALYST, ((BacteriaMutationRecipe) recipe).catalyst(), 1))));
        registry.addRecipeHandler(NTMenuTypes.BIO_REACTOR.get(), new MachineRecipeHandler<>(COLONY_FEEDING,
                (menu, recipe) -> feeding(menu.blockEntity, (ColonyFeedingRecipe) recipe)));
        registry.addRecipeHandler(NTMenuTypes.INDUSTRIAL_BIO_REACTOR.get(), new MachineRecipeHandler<>(COLONY_FEEDING,
                (menu, recipe) -> feeding(menu.blockEntity, (ColonyFeedingRecipe) recipe)));
        registry.addRecipeHandler(NTMenuTypes.COMBUSTION_DYNAMO.get(), new MachineRecipeHandler<>(COMBUSTION_ADDITIVE,
                (menu, recipe) -> List.of(new RecipeTransfer.Entry(CombustionDynamoBlockEntity.ADDITIVE_SLOT, ((CombustionAdditiveRecipe) recipe).ingredient(), 1))));
        registry.addRecipeHandler(NTMenuTypes.GRAFTING_STATION.get(), new MachineRecipeHandler<>(BACTERIA_GRAFTING,
                (menu, recipe) -> grafting((BacteriaGraftingEmiRecipe.Grafting) recipe)));
    }

    private static List<RecipeTransfer.Entry> counted(List<IngredientWithCount> ingredients, int slots) {
        List<RecipeTransfer.Entry> entries = new ArrayList<>();
        for (int i = 0; i < ingredients.size() && i < slots; i++) {
            IngredientWithCount ingredient = ingredients.get(i);
            if (!ingredient.ingredient().isEmpty()) {
                entries.add(new RecipeTransfer.Entry(i, ingredient.ingredient(), ingredient.count()));
            }
        }
        return entries;
    }

    private static List<RecipeTransfer.Entry> feeding(Object blockEntity, ColonyFeedingRecipe recipe) {
        if (!(blockEntity instanceof AbstractBioReactorBlockEntity reactor) || reactor.getNutrientSlotCount() == 0) {
            return List.of();
        }
        return List.of(new RecipeTransfer.Entry(reactor.nutrientSlot(0), recipe.ingredient().ingredient(), recipe.ingredient().count()));
    }

    private static List<RecipeTransfer.Entry> grafting(BacteriaGraftingEmiRecipe.Grafting recipe) {
        return List.of(new RecipeTransfer.Entry(GraftingStationBlockEntity.DISH_SLOT, Ingredient.of(NTItems.PETRI_DISH.get()), 1),
                new RecipeTransfer.Entry(GraftingStationBlockEntity.SAMPLE_SLOT, Ingredient.of(recipe.sample()), 1));
    }

    private static void exclusionAreas(Screen screen, Consumer<Bounds> consumer) {
        if (screen instanceof SideConfigHost host) {
            Rect2i area = host.sideConfigArea();
            if (area != null) {
                consumer.accept(bounds(area));
            }
        }
        if (screen instanceof ConfinedSpawnerScreen spawner) {
            for (Rect2i area : spawner.extraAreas()) {
                consumer.accept(bounds(area));
            }
        }
    }

    static Bounds bounds(Rect2i area) {
        return new Bounds(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }

    private static String path(ResourceKey<?> key) {
        return key.location().getNamespace() + "/" + key.location().getPath();
    }

    private static String path(BacteriaSelector selector) {
        return (selector.tag() ? "tag/" : "") + selector.id().getNamespace() + "/" + selector.id().getPath();
    }

    private static final class SyntheticIds {
        private final Set<String> used = new HashSet<>();

        private ResourceLocation next(String base) {
            return unique("/derived/" + base);
        }

        private ResourceLocation info(String base) {
            return unique("/info/" + base);
        }

        private ResourceLocation unique(String base) {
            String candidate = base;
            int suffix = 1;
            while (!used.add(candidate)) {
                candidate = base + "_" + suffix++;
            }
            return Nautec.rl(candidate);
        }
    }
}
