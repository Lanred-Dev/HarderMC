package com.hardermc.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import com.hardermc.HarderMC;

public class bdloc implements CommandExecutor {
    private final HarderMC plugin;

    public bdloc(HarderMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        if (!plugin.bossDungeonEvent.isActive()) {
            sender.sendMessage("There is no active Boss Dungeon.");
            return true;
        }

        Location location = plugin.bossDungeonEvent.location;
        sender.sendMessage(String.format("The Boss Dungeon is located at (%d, %d, %d).",
                location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return true;
    }
}