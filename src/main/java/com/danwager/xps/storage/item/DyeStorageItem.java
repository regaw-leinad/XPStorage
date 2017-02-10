package com.danwager.xps.storage.item;

import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public abstract class DyeStorageItem extends StorageItem {

    private final Dye dye;

    public DyeStorageItem(DyeColor dyeColor) {
        this.dye = new Dye(dyeColor);
    }

    @Override
    protected ItemStack getBaseItemStack() {
        return this.dye.toItemStack(1);
    }
}
