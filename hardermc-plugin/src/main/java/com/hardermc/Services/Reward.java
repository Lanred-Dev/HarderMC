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
                    Material.BREAD)),
            Map.entry(ItemTier.UNCOMMON, List.of(
                    Material.IRON_SWORD,
                    Material.BOW,
                    Material.IRON_HELMET,
                    Material.IRON_CHESTPLATE,
                    Material.IRON_LEGGINGS,
                    Material.IRON_BOOTS,
                    Material.GOLD_INGOT,
                    Material.COOKED_BEEF,
                    Material.CROSSBOW)),
            Map.entry(ItemTier.RARE, List.of(
                    Material.DIAMOND_SWORD,
                    Material.DIAMOND_HELMET,
                    Material.DIAMOND_CHESTPLATE,
                    Material.DIAMOND_LEGGINGS,
                    Material.DIAMOND_BOOTS,
                    Material.DIAMOND,
                    Material.EMERALD,
                    Material.SHULKER_BOX)),
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
                    Material.MACE)),
            Map.entry(ItemTier.LEGENDARY, List.of(
                    Material.ENCHANTED_GOLDEN_APPLE,
                    Material.TOTEM_OF_UNDYING,
                    Material.ELYTRA)));
    private final HarderMC plugin;

    public Reward(HarderMC plugin) {
        this.plugin = plugin;
    }

    private static enum ItemTier {
        COMMON(0.25), UNCOMMON(0.50), RARE(0.75), EPIC(1.00), LEGENDARY(1.50);

        private final double value;

        ItemTier(double value) {
            this.value = value;
        }

        public double value() {
            return value - 0.25;
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
        double shift = 0.15 * (multiplierLevel - 1);
        double legendaryTierValue = ItemTier.LEGENDARY.value();
        double roll = Math.min((Math.random() * legendaryTierValue) + shift, legendaryTierValue);

        ItemTier previous = null;

        for (ItemTier tier : ItemTier.values()) {
            double lower = previous == null ? 0 : previous.value();
            double upper = tier.value();

            if (roll <= upper && roll > lower)
                return tier;

            previous = tier;
        }

        return ItemTier.LEGENDARY;
    }
}
