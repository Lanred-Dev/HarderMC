package com.hardermc.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hardermc.HarderMC;

public class level implements CommandExecutor {
    private final HarderMC plugin;

    public level(HarderMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        sender.sendMessage(String.format("%d (%d%% difficulty)", plugin.levelSystem.level,
                (int) (plugin.levelSystem.levelMultiplier * 100)));
        return true;
    }
}