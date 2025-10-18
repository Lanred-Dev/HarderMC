package com.hardermc.Systems;

import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
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
    public final MultiplierGroup healthMultiplier = new MultiplierGroup(Utils.MAX_ENTITY_HEALTH);
    public final MultiplierGroup damageMultiplier = new MultiplierGroup(Utils.MAX_ENTITY_DAMAGE);
    public final MultiplierGroup speedMultiplier = new MultiplierGroup(Utils.MAX_ENTITY_SPEED);
    private final List<Pair<Attribute, MultiplierGroup>> MOB_ATTRIBUTES = List.of(
            new Pair<>(Attribute.GENERIC_MAX_HEALTH, healthMultiplier),
            new Pair<>(Attribute.GENERIC_ATTACK_DAMAGE, damageMultiplier),
            new Pair<>(Attribute.GENERIC_MOVEMENT_SPEED, speedMultiplier));
    private final EntityType[] SPAWNABLE_MOBS = {
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.CREEPER,
            EntityType.SPIDER,
            EntityType.ENDERMAN,
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
        if (!(event.getEntity() instanceof Monster mob))
            return;

        // Only modify naturally spawned mobs, otherwise infinite loops could occur
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            for (int index = 0; index < Math.floor(spawnRateMultiplier.getTotal()); index++) {
                event.getLocation().getWorld().spawnEntity(event.getLocation(),
                        Utils.randomEntryFromArray(SPAWNABLE_MOBS));
            }
        }

        for (Pair<Attribute, MultiplierGroup> attributePair : MOB_ATTRIBUTES) {
            mob.getAttribute(attributePair.first()).setBaseValue(
                    mob.getAttribute(attributePair.first()).getBaseValue()
                            * attributePair.second().getTotal());
        }

        mob.setCustomName(String.format("%d level %s", plugin.levelSystem.level,
                mob.getType().toString().toLowerCase().replace('_', ' ')));
    }
}