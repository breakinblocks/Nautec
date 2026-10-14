package com.breakinblocks.nautec.content.bacteria;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public final class BacteriaLineage {
    private BacteriaLineage() {
    }

    public record Path(ResourceKey<Bacteria> target, ResourceKey<Bacteria> root, List<BacteriaMutationRecipe> steps) {
        public double expectedTries() {
            double tries = 0;
            for (BacteriaMutationRecipe step : steps) {
                tries += tries(step);
            }
            return tries;
        }
    }

    private record Node(ResourceKey<Bacteria> strain, double cost) {
    }

    public static Set<ResourceKey<Bacteria>> graftable() {
        Set<ResourceKey<Bacteria>> roots = new HashSet<>();
        for (BacteriaObtainValue value : BuiltInRegistries.BLOCK.getDataMap(NTDataMaps.BACTERIA_OBTAINING).values()) {
            roots.add(value.bacteria());
        }
        return roots;
    }

    public static Map<ResourceKey<Bacteria>, Path> fastest(Collection<BacteriaMutationRecipe> mutations, Set<ResourceKey<Bacteria>> roots) {
        Map<ResourceKey<Bacteria>, List<BacteriaMutationRecipe>> edges = new HashMap<>();
        for (BacteriaMutationRecipe recipe : mutations) {
            if (!recipe.refines() && recipe.chance() > 0) {
                edges.computeIfAbsent(recipe.inputBacteria(), key -> new ArrayList<>()).add(recipe);
            }
        }
        Map<ResourceKey<Bacteria>, Double> cost = new HashMap<>();
        Map<ResourceKey<Bacteria>, BacteriaMutationRecipe> via = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(Node::cost)
                .thenComparing(node -> node.strain().location().toString()));
        for (ResourceKey<Bacteria> root : roots) {
            cost.put(root, 0D);
            queue.add(new Node(root, 0D));
        }
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node.cost() > cost.getOrDefault(node.strain(), Double.MAX_VALUE)) {
                continue;
            }
            for (BacteriaMutationRecipe edge : edges.getOrDefault(node.strain(), List.of())) {
                double next = node.cost() + tries(edge);
                if (next < cost.getOrDefault(edge.resultBacteria(), Double.MAX_VALUE)) {
                    cost.put(edge.resultBacteria(), next);
                    via.put(edge.resultBacteria(), edge);
                    queue.add(new Node(edge.resultBacteria(), next));
                }
            }
        }
        List<ResourceKey<Bacteria>> targets = new ArrayList<>(via.keySet());
        targets.removeAll(roots);
        targets.sort(Comparator.comparing(key -> key.location().toString()));
        Map<ResourceKey<Bacteria>, Path> paths = new LinkedHashMap<>();
        for (ResourceKey<Bacteria> target : targets) {
            List<BacteriaMutationRecipe> steps = new ArrayList<>();
            ResourceKey<Bacteria> cursor = target;
            while (!roots.contains(cursor)) {
                BacteriaMutationRecipe step = via.get(cursor);
                steps.add(step);
                cursor = step.inputBacteria();
            }
            Collections.reverse(steps);
            paths.put(target, new Path(target, cursor, List.copyOf(steps)));
        }
        return paths;
    }

    private static double tries(BacteriaMutationRecipe recipe) {
        return 100D / Math.min(100D, recipe.chance());
    }
}
