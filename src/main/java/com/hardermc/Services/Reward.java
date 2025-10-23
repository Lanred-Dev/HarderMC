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
    private static final Map<ItemTier, List<ItemStack>> ITEMS = Map.ofEntries(
            Map.entry(ItemTier.COMMON, List.of(
                    new ItemStack(Material.IRON_SWORD),
                    new ItemStack(Material.IRON_HELMET),
                    new ItemStack(Material.IRON_CHESTPLATE),
                    new ItemStack(Material.COOKED_BEEF, 5),
                    new ItemStack(Material.IRON_PICKAXE),
                    new ItemStack(Material.SHIELD))),
            Map.entry(ItemTier.UNCOMMON, List.of(
                    new ItemStack(Material.IRON_INGOT, 8),
                    new ItemStack(Material.IRON_LEGGINGS),
                    new ItemStack(Material.IRON_BOOTS),
                    new ItemStack(Material.CROSSBOW),
                    new ItemStack(Material.SHIELD))),
            Map.entry(ItemTier.RARE, List.of(
                    new ItemStack(Material.DIAMOND_SWORD),
                    new ItemStack(Material.DIAMOND_HELMET),
                    new ItemStack(Material.DIAMOND_CHESTPLATE),
                    new ItemStack(Material.DIAMOND_LEGGINGS),
                    new ItemStack(Material.DIAMOND_BOOTS),
                    new ItemStack(Material.DIAMOND, 3),
                    new ItemStack(Material.EMERALD, 3),
                    new ItemStack(Material.DIAMOND_PICKAXE))),
            Map.entry(ItemTier.EPIC, List.of(
                    new ItemStack(Material.NETHERITE_SWORD),
                    new ItemStack(Material.TRIDENT),
                    new ItemStack(Material.NETHERITE_HELMET),
                    new ItemStack(Material.NETHERITE_CHESTPLATE),
                    new ItemStack(Material.NETHERITE_LEGGINGS),
                    new ItemStack(Material.NETHERITE_BOOTS),
                    new ItemStack(Material.NETHERITE_INGOT, 2),
                    new ItemStack(Material.GOLDEN_APPLE, 2),
                    new ItemStack(Material.HEART_OF_THE_SEA),
                    new ItemStack(Material.CROSSBOW))),
            Map.entry(ItemTier.LEGENDARY, List.of(
                    new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
                    new ItemStack(Material.TOTEM_OF_UNDYING),
                    new ItemStack(Material.ELYTRA),
                    new ItemStack(Material.NETHERITE_SWORD),
                    new ItemStack(Material.NETHERITE_PICKAXE),
                    new ItemStack(Material.TRIDENT),
                    new ItemStack(Material.SHULKER_BOX))));
    private final HarderMC plugin;

    public Reward(HarderMC plugin) {
        this.plugin = plugin;
    }

    private static enum ItemTier {
        COMMON(0.40, -0.10),
        UNCOMMON(0.30, 0.00),
        RARE(0.20, 0.10),
        EPIC(0.08, 0.15),
        LEGENDARY(0.02, 0.10);

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
            ItemStack item = Utils.randomEntryFromList(ITEMS.get(rollForTier(multiplierLevel)));
            rewardedItems.add(item.clone());
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

        // This should not happen but just in case
        return ItemTier.COMMON;
    }
}
