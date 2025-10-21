package com.hardermc.Systems;

import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.hardermc.HarderMC;
import com.hardermc.Utils;
import com.hardermc.Objects.MultiplierGroup;
import com.hardermc.Objects.Pair;

public class MobHandler implements Listener {
    private final HarderMC plugin;
    public final MultiplierGroup globalMultiplier = new MultiplierGroup(3.0);
    public final MultiplierGroup spawnRateMultiplier = new MultiplierGroup(5.0);
    public final MultiplierGroup healthMultiplier = new MultiplierGroup(
            Utils.MAX_ATTRIBUTE_VALUES.get(Attribute.MAX_HEALTH));
    public final MultiplierGroup damageMultiplier = new MultiplierGroup(
            Utils.MAX_ATTRIBUTE_VALUES.get(Attribute.ATTACK_DAMAGE));
    public final MultiplierGroup speedMultiplier = new MultiplierGroup(
            Utils.MAX_ATTRIBUTE_VALUES.get(Attribute.MOVEMENT_SPEED));
    // This is not static because it uses instance variables
    private final List<Pair<Attribute, MultiplierGroup>> MULTIPLIED_MOB_ATTRIBUTES = List.of(
            new Pair<>(Attribute.MAX_HEALTH, healthMultiplier),
            new Pair<>(Attribute.ATTACK_DAMAGE, damageMultiplier),
            new Pair<>(Attribute.MOVEMENT_SPEED, speedMultiplier));
    private static final EntityType[] SPAWNABLE_MOB_TYPES = {
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.CREEPER,
            EntityType.SPIDER,
            EntityType.WITCH,
            EntityType.HUSK,
            EntityType.STRAY,
            EntityType.DROWNED,
    };

    public MobHandler(HarderMC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Monster) || entity.getPersistentDataContainer().has(plugin.bossDungeonEvent.BOSS_KEY))
            return;

        // Only modify naturally spawned mobs, otherwise infinite loops could occur
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            for (int index = 0; index < Math.floor(spawnRateMultiplier.getTotal()); index++) {
                event.getLocation().getWorld().spawnEntity(event.getLocation(),
                        Utils.randomEntryFromArray(SPAWNABLE_MOB_TYPES));
            }
        }

        for (Pair<Attribute, MultiplierGroup> attributePair : MULTIPLIED_MOB_ATTRIBUTES) {
            entity.getAttribute(attributePair.first()).setBaseValue(
                    entity.getAttribute(attributePair.first()).getBaseValue()
                            * attributePair.second().getTotal());
        }

        entity.setCustomName(String.format("Level %d %s", plugin.levelSystem.level,
                entity.getType().toString().toLowerCase().replace('_', ' ')));
    }
}