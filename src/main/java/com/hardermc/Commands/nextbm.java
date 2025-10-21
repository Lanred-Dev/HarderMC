package com.hardermc.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hardermc.HarderMC;

public class nextbm implements CommandExecutor {
    private final HarderMC plugin;

    public nextbm(HarderMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        if (plugin.bloodMoonEvent.isActive()) {
            sender.sendMessage("A Blood Moon is currently active.");
        } else {
            int daysUntil = plugin.scheduler.getDaysUntilEvent(plugin.bloodMoonEvent.EVENT_ID());
            sender.sendMessage(String.format("%d days until the next Blood Moon.", daysUntil));
        }

        return true;
    }
}