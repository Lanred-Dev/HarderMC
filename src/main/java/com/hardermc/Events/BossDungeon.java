package com.hardermc.Events;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.BlockVector;

import com.hardermc.HarderMC;
import com.hardermc.Utils;
import com.hardermc.Objects.Pair;
import com.hardermc.Objects.SchedulerEvent;
import com.hardermc.Systems.Scheduler.TimeOfDay;

public class BossDungeon extends SchedulerEvent implements Listener {
    public final NamespacedKey BOSS_KEY;
    private static final double ON_DEFEAT_REWARD_MULTIPLIER = 2.0;
    private static final String DUNGEON_STRUCTURE_FILE_NAME = "structures/boss_dungeon.nbt";
    private static final String DUNGEON_LOCATION_KEY = "BOSS_DUNGEON_LOCATION";
    private static final EntityType[] BOSS_TYPES = {
            EntityType.ZOMBIE,
    };
    private static final List<Pair<Attribute, Double>> MULTIPLIED_BOSS_ATTRIBUTES = List.of(
            new Pair<>(Attribute.MAX_HEALTH, 5.0),
            new Pair<>(Attribute.ATTACK_DAMAGE, 3.0),
            new Pair<>(Attribute.MOVEMENT_SPEED, 1.25),
            new Pair<>(Attribute.FOLLOW_RANGE, 5.0),
            new Pair<>(Attribute.KNOCKBACK_RESISTANCE, 5.0));
    private static final ItemStack[] OFFERINGS = {
            new ItemStack(Material.COPPER_INGOT, 64),
            new ItemStack(Material.COAL, 48),
            new ItemStack(Material.IRON_INGOT, 32),
            new ItemStack(Material.LAPIS_LAZULI, 24),
            new ItemStack(Material.GOLD_INGOT, 16),
            new ItemStack(Material.REDSTONE, 16),
            new ItemStack(Material.DIAMOND, 8),
            new ItemStack(Material.EMERALD, 4),
    };
    private final Structure dungeonStructure;
    private boolean hasBeaten;
    private boolean hasEnded;
    public Location location;
    private ItemStack offering;
    private Chest chest;
    private int offeringsInChest;

    public BossDungeon(HarderMC plugin) {
        super(plugin);
        BOSS_KEY = new NamespacedKey(plugin, "is_boss_entity");

        File dungeonStructureFile = new File(plugin.getDataFolder(), DUNGEON_STRUCTURE_FILE_NAME);

        if (!dungeonStructureFile.exists()) {
            plugin.saveResource(DUNGEON_STRUCTURE_FILE_NAME, false);
        }

        StructureManager structureManager = plugin.getServer().getStructureManager();
        Structure structure = null;

        try {
            structure = structureManager.loadStructure(dungeonStructureFile);
        } catch (Exception error) {
            error.printStackTrace();
            HarderMC.LOGGER.severe("Failed to load Boss Dungeon structure.");
        }

        dungeonStructure = structure;
        initialize();
    }

    @Override
    public void onDayPassed() {
        if (!isActive())
            return;

        Bukkit.broadcastMessage(
                String.format("The Boss Dungeon has %d days left.", plugin.scheduler.getDaysLeftInEvent(EVENT_ID())));
    }

    @Override
    public int EVENT_INTERVAL() {
        return 6;
    }

    @Override
    public TimeOfDay EVENT_STARTS_AT() {
        return TimeOfDay.DAY;
    }

    @Override
    public TimeOfDay EVENT_ENDS_AT() {
        return TimeOfDay.NIGHT;
    }

    @Override
    public String EVENT_ID() {
        return "BOSS_DUNGEON";
    }

    @Override
    public int EVENT_LASTS_FOR() {
        return 3;
    }

    @Override
    public int EVENT_DELAY_BEFORE_FIRST_START() {
        return 1;
    }

    @Override
    public void start() {
        hasBeaten = false;
        hasEnded = false;
        offeringsInChest = 0;

        if (plugin.serverDataService.has(DUNGEON_LOCATION_KEY)) {
            @SuppressWarnings("unchecked")
            ArrayList<Double> savedLocation = (ArrayList<Double>) plugin.serverDataService.get(DUNGEON_LOCATION_KEY,
                    null);
            location = new Location(plugin.getServer().getWorld("world"), savedLocation.get(0), savedLocation.get(1),
                    savedLocation.get(2));
        }

        spawnDungeonStructure(location);

        Bukkit.broadcastMessage(
                String.format("A Boss Dungeon has appeared at (%d, %d, %d) and will expire in %d days.",
                        location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                        EVENT_LASTS_FOR()));
    }

    @Override
    public void end() {
        if (hasEnded)
            return;

        hasEnded = true;
        plugin.serverDataService.remove(DUNGEON_LOCATION_KEY);

        if (!hasBeaten) {
            Bukkit.broadcastMessage("The Boss Dungeon has vanished...");
        } else {
            Bukkit.broadcastMessage("The Boss has been defeated.");
        }
    }

    private void spawnDungeonStructure(Location forcedLocation) {
        // This should never happen, but just in case
        if (dungeonStructure == null)
            return;

        World world = plugin.getServer().getWorld("world");

        if (world == null)
            return;

        offering = Utils.randomEntryFromArray(OFFERINGS);

        if (forcedLocation != null) {
            location = forcedLocation;
        } else {
            location = Utils.getRandomPositionAroundPlayer(
                    Utils.randomEntryFromArray(Bukkit.getOnlinePlayers().toArray(new Player[0])),
                    1500,
                    100,
                    -30,
                    35);
        }

        plugin.serverDataService.set(DUNGEON_LOCATION_KEY, new double[] {
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        });
        dungeonStructure.place(
                location,
                false,
                StructureRotation.NONE,
                Mirror.NONE,
                0,
                1.0f,
                new Random());

        BlockVector structureSize = dungeonStructure.getSize();

        all: for (int x = 0; x < structureSize.getX(); x++) {
            for (int y = 0; y < structureSize.getY(); y++) {
                for (int z = 0; z < structureSize.getZ(); z++) {
                    Block block = world.getBlockAt(
                            location.getBlockX() + x,
                            location.getBlockY() + y,
                            location.getBlockZ() + z);

                    Material type = block.getType();
                    if (type == Material.WATER || type == Material.LAVA)
                        block.setType(Material.AIR);

                    if (!(block.getState() instanceof Chest chest))
                        continue;

                    this.chest = chest;

                    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta meta = (BookMeta) book.getItemMeta();
                    meta.setTitle("Dungeon Requirements");
                    meta.addPage(String.format("This dungeon requires: %d %s", offering.getAmount(),
                            offering.getType().toString()));
                    book.setItemMeta(meta);
                    chest.getInventory().addItem(book);

                    break all;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory chestInventory = event.getInventory();

        if (!isActive() || chest == null || !chestInventory.getLocation().equals(chest.getLocation()))
            return;

        ItemStack placedItem = null;

        if (event.isShiftClick() && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            placedItem = event.getCurrentItem();
        } else if (event.getCursor() != null && event.getRawSlot() < chestInventory.getSize()) {
            placedItem = event.getCursor();
        }

        if (placedItem == null || !placedItem.isSimilar(offering))
            return;

        // Delay by one tick to allow the item to be placed in the chest
        Bukkit.getScheduler().runTask(plugin, () -> {
            offeringsInChest = 0;

            for (ItemStack item : chestInventory.getContents()) {
                if (item == null || !item.isSimilar(offering))
                    continue;

                offeringsInChest += item.getAmount();
            }

            if (offeringsInChest >= offering.getAmount()) {
                int amountToRemove = offering.getAmount();

                for (int slot = 0; slot < chestInventory.getSize(); slot++) {
                    ItemStack item = chestInventory.getItem(slot);

                    if (item == null || !item.isSimilar(offering))
                        continue;

                    if (item.getAmount() > amountToRemove) {
                        item.setAmount(item.getAmount() - amountToRemove);
                        chestInventory.setItem(slot, item);
                        break;
                    } else {
                        amountToRemove -= item.getAmount();
                        chestInventory.setItem(slot, null);
                    }

                    if (amountToRemove <= 0)
                        break;
                }

                spawnBoss();
            } else {
                Bukkit.broadcastMessage(String.format("%d more offerings needed to start Boss Dungeon.",
                        offering.getAmount() - offeringsInChest));
            }
        });
    }

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!isActive() || !entity.getPersistentDataContainer().has(BOSS_KEY))
            return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        for (ItemStack reward : plugin.rewardService.getRewards(ON_DEFEAT_REWARD_MULTIPLIER)) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), reward);
        }

        hasBeaten = true;
        end();
    }

    private void spawnBoss() {
        World world = plugin.getServer().getWorld("world");

        if (world == null)
            return;

        HarderMC.LOGGER.info("Spawning dungeon boss");
        Bukkit.broadcastMessage("The Boss has been summoned...");

        for (int index = 0; index < Bukkit.getOnlinePlayers().size(); index++) {
            BlockVector structureSize = dungeonStructure.getSize();
            Location spawnLocation = location.clone().add(structureSize.getX() / 2, 5, structureSize.getZ() / 2);
            EntityType bossType = Utils.randomEntryFromArray(BOSS_TYPES);

            LivingEntity boss = (LivingEntity) world.spawnEntity(spawnLocation, bossType);
            boss.addPotionEffect(
                    new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
            boss.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 1, false, false));
            boss.getPersistentDataContainer().set(BOSS_KEY, PersistentDataType.BOOLEAN, true);
            boss.setCustomName(String.format("Level %d Dungeon Boss", plugin.levelSystem.level));
            boss.setCustomNameVisible(true);

            for (Pair<Attribute, Double> pair : MULTIPLIED_BOSS_ATTRIBUTES) {
                Attribute attribute = pair.first();
                Double multiplier = pair.second();
                boss.getAttribute(attribute).setBaseValue(
                        Utils.clamp(boss.getAttribute(attribute).getBaseValue()
                                * (multiplier + plugin.levelSystem.level / 10.0), 0,
                                Utils.MAX_ATTRIBUTE_VALUES.get(attribute)));
            }

            boss.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            boss.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            boss.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            boss.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            boss.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
            boss.getEquipment().setHelmetDropChance(0f);
            boss.getEquipment().setChestplateDropChance(0f);
            boss.getEquipment().setLeggingsDropChance(0f);
            boss.getEquipment().setBootsDropChance(0f);
            boss.getEquipment().setItemInMainHandDropChance(0f);
        }
    }
}