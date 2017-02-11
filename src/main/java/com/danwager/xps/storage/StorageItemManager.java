package com.danwager.xps.storage;

import com.danwager.xps.XPUtil;
import com.danwager.xps.storage.item.BasicStorageItem;
import com.danwager.xps.storage.item.ItemData;
import com.danwager.xps.storage.item.StorageItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages the items that store player's XP
 */
public class StorageItemManager implements Listener {

    // Other items for future
    // Dye: gray, (green), pink, magenta, purple
    // clay ball, firework star, slime ball
    private ItemStack storageItem;

    public StorageItemManager(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.storageItem = new BasicStorageItem().getStorageItem(new ItemData());
        registerRecipe();
    }

    private void registerRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(this.storageItem);
        recipe.shape("III", "IBI", "ICI");
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('B', Material.ENCHANTED_BOOK);
        recipe.setIngredient('C', Material.CHEST);
        Bukkit.addRecipe(recipe);
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
                // If the player has no levels
                if (currentLevel == 0 && levelProgress == 0) {
                    playNoLevelsToStoreSound(player);
                    return;
                }

                if (itemData.isFull()) {
                    playStorageFullSound(player);
                    return;
                }

                storeXp(player, itemData, item);
                break;
            case LEFT_CLICK_BLOCK:
                //cancelEvent(event);
            case LEFT_CLICK_AIR:
                // Restore XP
                if (itemData.isEmpty()) {
                    playStorageEmptySound(player);
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
            // No levels to store
            if (currentLevel == 0) {
                playNoLevelsToStoreSound(player);
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

        itemData.storeXP(xpToStore);

        itemData.applyToItem(item);
        player.giveExpLevels(levelChange);
        player.setExp(newLevelProgress);

        playStoreSound(player);
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

        itemData.giveXp(xpToGive);

        itemData.applyToItem(item);
        player.giveExpLevels(levelChange);
        player.setExp(newLevelProgress);

        playGiveSound(player);
    }

    private void playStoreSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.25f, 1.5f);
    }

    private void playGiveSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 1f);
    }

    private void playStorageFullSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.25f, 1.5f);
    }

    private void playStorageEmptySound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.25f, 1.5f);
    }

    private void playNoLevelsToStoreSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.25f, 1.5f);
    }
}
