package com.danwager.xps;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.plugin.java.JavaPlugin;

public class XPStorage extends JavaPlugin implements Listener {

    // Other items for future
    // Dye: gray, (green), pink, magenta, purple
    // clay ball, firework star, slime ball

    // Lime Dye
    private ItemStack storageItem;

    @Override
    public void onEnable() {
        generateItems();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void generateItems() {
        this.storageItem = new Dye(DyeColor.LIME).toItemStack(1);
        applyEnchant(this.storageItem);
        applyName(this.storageItem);
    }

    private void applyEnchant(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    private void applyName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "XP Storage");
        item.setItemMeta(meta);
    }

    /*@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.getInventory().setItem(0, this.storageItem.clone());
        player.getInventory().setHeldItemSlot(0);
    }*/

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.PHYSICAL) {
            return;
        }

        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        // If it's a storage storageItem
        if (!item.getItemMeta().hasEnchants()) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        Player player = event.getPlayer();
        int xpLevel = player.getLevel();
        int amountToChange = player.isSneaking() ? 5 : 1;

        switch (action) {
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                if (xpLevel < amountToChange) {
                    player.sendMessage(ChatColor.RED + "You don't have " + amountToChange + " XP to store!");
                    return;
                }

                player.giveExpLevels(-amountToChange);

                break;
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                player.giveExpLevels(amountToChange);
                break;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player)sender;

        int currentLevel = player.getLevel();
        float xpPercent = player.getExp();
        int amount = XPUtil.getTotalXpToRemove(currentLevel, xpPercent);

        player.sendMessage(ChatColor.GREEN + "Removing " + amount + " xp");

        player.giveExpLevels(-1);
        player.setExp(currentLevel == 0 ? 0 : xpPercent);

        return true;
    }
}
