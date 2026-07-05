package ru.mrsteve.limbobots;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Когда на сервер заходит настоящий игрок, ему нужно отдельно
 * отправить пакеты появления всех уже существующих ботов —
 * иначе он не увидит их ни в табе, ни в мире.
 */
public class PlayerJoinListener implements Listener {

    private final LimboBots plugin;

    public PlayerJoinListener(LimboBots plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Небольшая задержка, чтобы клиент успел полностью прогрузиться
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Bot bot : plugin.getBotManager().getAllBots()) {
                bot.sendSpawnPackets(event.getPlayer());
            }
        }, 10L);
    }
}
