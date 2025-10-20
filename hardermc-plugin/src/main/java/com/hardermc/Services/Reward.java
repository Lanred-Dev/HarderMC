package com.hardermc.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.hardermc.HarderMC;
import com.hardermc.Utils;

public class Reward implements Listener {
    private static final int BASE_REWARD_COUNT = 4;
    private static final double BASE_REWARD_MULTIPLIER = 0.3;
    private static final Map<ItemTier, List<Material>> ITEMS = Map.ofEntries(
            Map.entry(ItemTier.COMMON, List.of(
                    Material.WOODEN_SWORD,
                    Material.STONE_SWORD,
                    Material.LEATHER_HELMET,
                    Material.LEATHER_CHESTPLATE,
                    Material.LEATHER_LEGGINGS,
                    Material.LEATHER_BOOTS,
                    Material.IRON_INGOT,
                    Material.BREAD,
                    Material.STONE_PICKAXE,
                    Material.APPLE)),
            Map.entry(ItemTier.UNCOMMON, List.of(
                    Material.IRON_SWORD,
                    Material.BOW,
                    Material.IRON_HELMET,
                    Material.IRON_CHESTPLATE,
                    Material.IRON_LEGGINGS,
                    Material.IRON_BOOTS,
                    Material.GOLD_INGOT,
                    Material.COOKED_BEEF,
                    Material.CROSSBOW,
                    Material.IRON_PICKAXE,
                    Material.SHIELD)),
            Map.entry(ItemTier.RARE, List.of(
                    Material.DIAMOND_SWORD,
                    Material.DIAMOND_HELMET,
                    Material.DIAMOND_CHESTPLATE,
                    Material.DIAMOND_LEGGINGS,
                    Material.DIAMOND_BOOTS,
                    Material.DIAMOND,
                    Material.EMERALD,
                    Material.SHULKER_BOX,
                    Material.DIAMOND_PICKAXE,
                    Material.TOTEM_OF_UNDYING)),
            Map.entry(ItemTier.EPIC, List.of(
                    Material.NETHERITE_SWORD,
                    Material.TRIDENT,
                    Material.NETHERITE_HELMET,
                    Material.NETHERITE_CHESTPLATE,
                    Material.NETHERITE_LEGGINGS,
                    Material.NETHERITE_BOOTS,
                    Material.NETHERITE_INGOT,
                    Material.GOLDEN_APPLE,
                    Material.HEART_OF_THE_SEA,
                    Material.CROSSBOW,
                    Material.SHULKER_BOX)),
            Map.entry(ItemTier.LEGENDARY, List.of(
                    Material.ENCHANTED_GOLDEN_APPLE,
                    Material.TOTEM_OF_UNDYING,
                    Material.ELYTRA,
                    Material.NETHERITE_SWORD,
                    Material.NETHERITE_PICKAXE,
                    Material.TRIDENT)));
    private final HarderMC plugin;

    public Reward(HarderMC plugin) {
        this.plugin = plugin;
    }

    private static enum ItemTier {
        COMMON(0.49, -0.2),
        UNCOMMON(0.30, 0.05),
        RARE(0.40, 0.15),
        EPIC(0.10, 0.3),
        LEGENDARY(0.01, 0.5);

        private final double chance;
        private final double multiplierWeight;

        ItemTier(double chance, double multiplierWeight) {
            this.chance = chance;
            this.multiplierWeight = multiplierWeight;
        }

        public double chance() {
            return chance;
        }

        public double multiplierWeight() {
            return multiplierWeight;
        }
    }

    public List<ItemStack> getRewards(double multiplierLevel, int rewardCount) {
        List<ItemStack> rewardedItems = new ArrayList<>();

        for (int i = 0; i < rewardCount; i++) {
            Material item = Utils.randomEntryFromList(ITEMS.get(rollForTier(multiplierLevel)));
            rewardedItems.add(new ItemStack(item, 1));
        }

        return rewardedItems;
    }

    public List<ItemStack> getRewards(double multiplierLevel) {
        multiplierLevel = multiplierLevel * plugin.levelSystem.levelMultiplier * BASE_REWARD_MULTIPLIER;
        return getRewards(multiplierLevel,
                Math.max(BASE_REWARD_COUNT, (int) Math.floor(BASE_REWARD_COUNT * multiplierLevel)));
    }

    private static ItemTier rollForTier(double multiplierLevel) {
        double[] scaledChances = new double[ItemTier.values().length];

        for (int index = 0; index < ItemTier.values().length; index++) {
            ItemTier tier = ItemTier.values()[index];
            scaledChances[index] = tier.chance() * Math.pow(multiplierLevel, tier.multiplierWeight());
        }

        double roll = Math.random() * Utils.sumArray(scaledChances);
        double cumulativeChance = 0;

        for (int index = 0; index < ItemTier.values().length; index++) {
            ItemTier tier = ItemTier.values()[index];
            cumulativeChance += scaledChances[index];

            if (roll <= cumulativeChance)
                return tier;
        }

        // This should not happen but just incase
        return ItemTier.COMMON;
    }
}
