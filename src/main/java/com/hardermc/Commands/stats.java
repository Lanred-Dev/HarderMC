package com.hardermc.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hardermc.HarderMC;

public class stats implements CommandExecutor {
    private final HarderMC plugin;

    public stats(HarderMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        for (Player player : Bukkit.getOnlinePlayers()) {
            int kills = plugin.playerStatTrackerSystem.kills.getOrDefault(player, 0);
            int deaths = plugin.playerStatTrackerSystem.deaths.getOrDefault(player, 0);
            sender.sendMessage(String.format("%s: %d Kills and %d Deaths", player.getName(), kills, deaths));
        }

        return true;
    }
}