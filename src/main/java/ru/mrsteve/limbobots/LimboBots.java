package ru.mrsteve.limbobots;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * LimboBots — плагин для Spigot/Paper 1.16.5
 * Автор: MrSteve
 *
 * Добавляет фейковых игроков (ботов), которые:
 *  - отображаются в табе (списке игроков);
 *  - видны в мире как NPC со скином игрока;
 *  - умеют "ходить" по случайным точкам рядом со спавном;
 *  - периодически пишут сообщения в чат;
 *  - на них можно телепортироваться и надевать броню/предметы через команды.
 */
public class LimboBots extends JavaPlugin {

    private BotManager botManager;

    @Override
    public void onEnable() {
        this.botManager = new BotManager(this);

        getCommand("limbobots").setExecutor(new LimboBotsCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(this), this);

        getLogger().info("========================================");
        getLogger().info(" LimboBots v" + getDescription().getVersion() + " включен!");
        getLogger().info(" Автор: MrSteve");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        if (botManager != null) {
            botManager.clearBots();
        }
        getLogger().info("LimboBots выключен, все боты удалены.");
    }

    public BotManager getBotManager() {
        return botManager;
    }
}
