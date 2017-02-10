package com.danwager.xps.storage.item;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class StorageItem {

    public static final String DISPLAY_NAME = ChatColor.GREEN + "XP Storage";

    protected abstract ItemStack getBaseItemStack();

    public final ItemStack getStorageItem(ItemData itemData) {
        ItemStack result = getBaseItemStack();

        result.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ItemMeta meta = result.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(DISPLAY_NAME);
        result.setItemMeta(meta);

        itemData.applyToItem(result);

        return result;
    }
}
