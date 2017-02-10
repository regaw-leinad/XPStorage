package com.danwager.xps;

import com.danwager.xps.storage.StorageItemManager;
import org.bukkit.plugin.java.JavaPlugin;

public class XPStorage extends JavaPlugin {

    @Override
    public void onEnable() {
        new StorageItemManager(this);
    }
}
