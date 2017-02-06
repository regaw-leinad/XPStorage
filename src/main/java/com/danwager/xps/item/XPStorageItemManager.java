package com.danwager.xps.item;

import com.danwager.xps.XPUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

/**
 * Manages the items that store player's XP
 */
public class XPStorageItemManager implements Listener {

    private static final String TEMPLATE_STORED = ChatColor.AQUA + "Stored:" + ChatColor.GRAY + " %d XP";

    private final JavaPlugin plugin;

    // Other items for future
    // Dye: gray, (green), pink, magenta, purple
    // clay ball, firework star, slime ball
    private ItemStack storageItem;

    public XPStorageItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);

        this.storageItem = new Dye(DyeColor.LIME).toItemStack(1);
        applyDefaults(this.storageItem);
    }

    /**
     * Gets a value indicating if the specified item is an xp storage item
     *
     * @param item The item to check
     * @return True if the item is an xp storage item, otherwise false
     */
    public boolean isXpStorageItem(ItemStack item) {
        if (item == null) {
            return false;
        }

        if (item.getType() != this.storageItem.getType()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getDisplayName().equals(this.storageItem.getItemMeta().getDisplayName())
                && meta.hasEnchant(Enchantment.DURABILITY)
                && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
    }

    /**
     * Applies the default values and properties to the specified xp storage item
     *
     * @param item The item to set the defaults on
     */
    private void applyDefaults(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ItemMeta meta = item.getItemMeta();

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.GREEN + "XP Storage");
        meta.setLore(Collections.singletonList(String.format(TEMPLATE_STORED, 0)));

        item.setItemMeta(meta);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.PHYSICAL) {
            return;
        }

        ItemStack item = event.getItem();

        if (!isXpStorageItem(item)) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        Player player = event.getPlayer();

        String storedLine = ChatColor.stripColor(item.getItemMeta().getLore().get(0));
        int storedXp = Integer.parseInt(storedLine.replaceAll("\\D", ""));

        int currentLevel = player.getLevel();
        float levelProgress = player.getExp();
        int levelsToTake = 1;

        switch (action) {
            // Store xp
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                int amount = XPUtil.getXpToRemove(currentLevel, levelProgress, levelsToTake);

                ItemMeta meta = item.getItemMeta();
                meta.setLore(Collections.singletonList(String.format(TEMPLATE_STORED, (storedXp + amount))));
                item.setItemMeta(meta);

                player.giveExpLevels(-levelsToTake);
                player.setExp(currentLevel < levelsToTake ? 0 : levelProgress);

                break;
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                if (storedXp <= 0) {
                    player.sendMessage(ChatColor.RED + "No XP left to give");
                    return;
                }

                break;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.getInventory().setItem(0, this.storageItem.clone());
        player.getInventory().setHeldItemSlot(0);
    }
}
