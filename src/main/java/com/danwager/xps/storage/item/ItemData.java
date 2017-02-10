package com.danwager.xps.storage.item;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemData {

    private static final String TEMPLATE_STORED_XP = ChatColor.AQUA + "Stored:" + ChatColor.GRAY + " %d XP";
    private static final String TEMPLATE_MAX_XP = ChatColor.AQUA + "Capacity:" + ChatColor.GRAY + " %d XP";

    private ItemLevel level;
    private int currentXP;

    public ItemData() {
        this(ItemLevel.BASIC, 0);
    }

    private ItemData(ItemLevel level, int currentXP) {
        this.level = level;
        this.currentXP = currentXP;
    }

    public ItemLevel getLevel() {
        return this.level;
    }

    public void setLevel(ItemLevel level) {
        this.level = level;
    }

    public int getStoredXP() {
        return this.currentXP;
    }

    public boolean storeXP(int amount) {
        int newAmount = getStoredXP() + amount;

        if (newAmount < 0 || newAmount > getMaxXP()) {
            return false;
        }

        this.currentXP = newAmount;
        return true;
    }

    public boolean giveXp(int amount) {
        return storeXP(-amount);
    }

    public int getRemainingSpace() {
        return getMaxXP() - getStoredXP();
    }

    public boolean hasSpaceFor(int amount) {
        return getRemainingSpace() >= amount;
    }

    public int getMaxXP() {
        return this.level.maxXP;
    }

    public void applyToItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        meta.setLore(Arrays.asList(
                String.format(TEMPLATE_STORED_XP, getStoredXP()),
                String.format(TEMPLATE_MAX_XP, getMaxXP())
        ));

        item.setItemMeta(meta);
    }

    public static ItemData fromItem(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();

        ItemLevel level = ItemLevel.fromXP(getAmountFromLore(lore.get(1)));
        int currentXP = getAmountFromLore(lore.get(0));

        return new ItemData(level, currentXP);
    }

    private static int getAmountFromLore(String line) {
        return Integer.parseInt(ChatColor.stripColor(line).replaceAll("\\D", ""));
    }

    private enum ItemLevel {
        BASIC(55);

        private static final Map<Integer, ItemLevel> levelMapping;

        static {
            levelMapping = new HashMap<>();

            for (ItemLevel itemLevel : values()) {
                levelMapping.put(itemLevel.maxXP, itemLevel);
            }
        }

        static ItemLevel fromXP(int amount) {
            return levelMapping.get(amount);
        }

        private final int maxXP;

        ItemLevel(int maxXP) {
            this.maxXP = maxXP;
        }
    }
}
