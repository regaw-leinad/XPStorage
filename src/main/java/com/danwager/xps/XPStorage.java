package com.danwager.xps;

import com.danwager.xps.storage.StorageItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class XPStorage extends JavaPlugin {

    @Override
    public void onEnable() {
        new StorageItemManager(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player)sender;

        player.sendMessage("xp: " + player.getExp());
        player.sendMessage("level: " + player.getLevel());

        return true;
    }
}
