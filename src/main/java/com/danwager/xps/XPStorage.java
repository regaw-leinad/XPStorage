package com.danwager.xps;

import com.danwager.xps.item.XPStorageItemManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class XPStorage extends JavaPlugin {

    private XPStorageItemManager manager;

    @Override
    public void onEnable() {
        this.manager = new XPStorageItemManager(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player)sender;

        int levelsToTake = Integer.parseInt(args[0]);
        int currentLevel = player.getLevel();
        float xpPercent = player.getExp();
        int amount = XPUtil.getXpToRemove(currentLevel, xpPercent, levelsToTake);

        player.sendMessage(ChatColor.GREEN + "Removing " + amount + " xp");

        player.giveExpLevels(-levelsToTake);
        player.setExp(currentLevel == 0 ? 0 : xpPercent);

        return true;
    }
}
