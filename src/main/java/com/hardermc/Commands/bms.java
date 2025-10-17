package com.hardermc.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hardermc.HarderMC;

public class bms implements CommandExecutor {
    private final HarderMC plugin;

    public bms(HarderMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        int kills = plugin.playerStatTracker.totalKills.getOrDefault(player, 0);
        int deaths = plugin.playerStatTracker.totalDeaths.getOrDefault(player, 0);
        sender.sendMessage(String.format("All time blood moon stats:\n%d kills, %d deaths", kills, deaths));
        return true;
    }
}
