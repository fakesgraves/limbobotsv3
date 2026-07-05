package ru.mrsteve.limbobots;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * Немного "накручивает" отображаемый онлайн в списке серверов (server list ping).
 *
 * ВАЖНО: стандартный Bukkit/Spigot ServerListPingEvent позволяет менять только
 * maxPlayers, а количество online-игроков (numPlayers) вычисляется сервером
 * автоматически из реального Bukkit.getOnlinePlayers() и не может быть
 * подменено через чистый Spigot API.
 *
 * Если сервер работает на Paper, для полноценной подмены онлайна в пинге
 * стоит использовать com.destroystokyo.paper.event.server.PaperServerListPingEvent,
 * у которого есть метод setNumPlayers(int) — это можно добавить отдельным
 * классом-листенером, если PaperServerListPingEvent доступен в classpath.
 *
 * Основная "накрутка онлайна" в этом плагине реализована через таб-лист:
 * боты реально добавляются в PacketPlayOutPlayerInfo и видны каждому
 * игроку в списке игроков (Tab), что визуально создаёт эффект заполненного сервера.
 */
public class ServerPingListener implements Listener {

    private final LimboBots plugin;

    public ServerPingListener(LimboBots plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        int botCount = plugin.getBotManager().getBotCount();
        if (botCount <= 0) return;

        int desiredMax = event.getNumPlayers() + botCount;
        if (event.getMaxPlayers() < desiredMax) {
            event.setMaxPlayers(desiredMax);
        }
    }
}
