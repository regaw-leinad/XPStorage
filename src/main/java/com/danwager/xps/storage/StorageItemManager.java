package com.danwager.xps.storage;

import com.danwager.xps.XPUtil;
import com.danwager.xps.storage.item.BasicStorageItem;
import com.danwager.xps.storage.item.ItemData;
import com.danwager.xps.storage.item.StorageItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages the items that store player's XP
 */
public class StorageItemManager implements Listener {

    private static final String TEMPLATE_STORED = ChatColor.AQUA + "Stored:" + ChatColor.GRAY + " %d XP";
    private static final String TEMPLATE_MAX = ChatColor.AQUA + "Capacity:" + ChatColor.GRAY + " %d XP";

    // Other items for future
    // Dye: gray, (green), pink, magenta, purple
    // clay ball, firework star, slime ball
    private ItemStack storageItem;

    public StorageItemManager(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.storageItem = new BasicStorageItem().getStorageItem(new ItemData());
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

        // TODO: Modify for multiple item types
        if (item.getType() != this.storageItem.getType()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getDisplayName().equals(StorageItem.DISPLAY_NAME)
                && meta.hasEnchant(Enchantment.DURABILITY)
                && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
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

        Player player = event.getPlayer();

        ItemData itemData = ItemData.fromItem(item);

        int currentLevel = player.getLevel();
        float levelProgress = player.getExp();

        switch (action) {
            case RIGHT_CLICK_BLOCK:
                cancelEvent(event);
            case RIGHT_CLICK_AIR:
                // Store XP
                if (currentLevel == 0 && levelProgress == 0) {
                    return;
                }

                if (itemData.getRemainingSpace() <= 0) {
                    player.sendMessage(ChatColor.RED + "No space to store XP");
                    return;
                }

                storeXp(player, itemData, item);
                break;
            case LEFT_CLICK_BLOCK:
                cancelEvent(event);
            case LEFT_CLICK_AIR:
                // Restore XP
                if (itemData.getStoredXP() == 0) {
                    player.sendMessage(ChatColor.RED + "No XP left to give");
                    return;
                }

                giveXp(player, itemData, item);

                break;
        }
    }

    private void cancelEvent(PlayerInteractEvent event) {
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);
    }

    private void storeXp(Player player, ItemData itemData, ItemStack item) {
        int currentLevel = player.getLevel();
        float levelProgress = player.getExp();

        int xpToStore = XPUtil.getXpRequiredFromLevel(currentLevel, levelProgress);

        float newLevelProgress = 0.0f;
        int levelChange = 0;

        boolean takePartialLevel = xpToStore >= 1;

        if (takePartialLevel) {
            // Partial level take
            if (!itemData.hasSpaceFor(xpToStore)) {
                int xpForLevel = XPUtil.getXpRequiredFromLevel(currentLevel);
                int unstored = xpToStore - itemData.getRemainingSpace();

                xpToStore = itemData.getRemainingSpace();

                // Set the new level progress to how much we need to take
                newLevelProgress = ((float)unstored) / ((float)xpForLevel);
            }
        } else {
            if (currentLevel == 0) {
                player.sendMessage(ChatColor.RED + "No XP to store");
                return;
            }

            // Get the full level xp amount to take instead
            xpToStore = XPUtil.getXpRequiredToLevel(currentLevel);

            if (!itemData.hasSpaceFor(xpToStore)) {
                int xpForLevel = XPUtil.getXpRequiredToLevel(currentLevel);
                int unstored = xpToStore - itemData.getRemainingSpace();

                xpToStore = itemData.getRemainingSpace();

                // Set the new level progress to how much we need to take
                newLevelProgress = ((float)unstored) / ((float)xpForLevel);
            }

            levelChange = -1;
        }

        boolean stored = itemData.storeXP(xpToStore);
        if (!stored) {
            player.sendMessage(ChatColor.RED + "Error storing XP");
            return;
        }

        itemData.applyToItem(item);
        player.giveExpLevels(levelChange);
        player.setExp(newLevelProgress);
    }

    private void giveXp(Player player, ItemData itemData, ItemStack item) {
        int currentLevel = player.getLevel();
        float levelProgress = player.getExp();

        int xpForLevel = XPUtil.getXpRequiredFromLevel(currentLevel);
        int xpToGive = xpForLevel - (int)(xpForLevel * levelProgress);

        int levelChange = 1;
        float newLevelProgress = 0.0f;

        if (itemData.getStoredXP() < xpToGive) {
            xpToGive = itemData.getStoredXP();
            levelChange = 0;
            newLevelProgress = ((float)xpToGive) / ((float)xpForLevel);
        }

        boolean given = itemData.giveXp(xpToGive);
        if (!given) {
            player.sendMessage(ChatColor.RED + "Error giving XP");
            return;
        }

        itemData.applyToItem(item);
        player.giveExpLevels(levelChange);
        player.setExp(newLevelProgress);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.getInventory().setItem(0, this.storageItem.clone());
        player.getInventory().setHeldItemSlot(0);
    }
}
