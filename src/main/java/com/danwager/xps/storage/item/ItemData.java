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
    private int xpStored;

    public ItemData() {
        this(ItemLevel.BASIC, 0);
    }

    private ItemData(ItemLevel level, int xpStored) {
        this.level = level;
        this.xpStored = xpStored;
    }

    public int getXpStored() {
        return this.xpStored;
    }

    public boolean storeXp(int amount) {
        int newAmount = getXpStored() + amount;

        if (newAmount < 0 || newAmount > getCapacity()) {
            return false;
        }

        this.xpStored = newAmount;
        return true;
    }

    public boolean giveXp(int amount) {
        return storeXp(-amount);
    }

    public int getRemainingSpace() {
        return getCapacity() - getXpStored();
    }

    public boolean hasSpaceFor(int amount) {
        return getRemainingSpace() >= amount;
    }

    public boolean isEmpty() {
        return getXpStored() == 0;
    }

    public boolean isFull() {
        return getXpStored() == getCapacity();
    }

    public int getCapacity() {
        return this.level.capacity;
    }

    public void applyToItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        meta.setLore(Arrays.asList(
                String.format(TEMPLATE_STORED_XP, getXpStored()),
                String.format(TEMPLATE_MAX_XP, getCapacity())
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
        BASIC(1395);

        private static final Map<Integer, ItemLevel> levelMapping;

        static {
            levelMapping = new HashMap<>();

            for (ItemLevel itemLevel : values()) {
                levelMapping.put(itemLevel.capacity, itemLevel);
            }
        }

        static ItemLevel fromXP(int amount) {
            return levelMapping.get(amount);
        }

        private final int capacity;

        ItemLevel(int capacity) {
            this.capacity = capacity;
        }
    }
}
