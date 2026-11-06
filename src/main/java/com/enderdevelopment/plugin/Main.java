package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private SandCastleManager castleManager;

    @Override
    public void onEnable() {
        castleManager = new SandCastleManager(this);
        Bukkit.getPluginManager().registerEvents(castleManager, this);
    }

    @Override
    public void onDisable() {
        castleManager.removeAllCastles();
    }
}
