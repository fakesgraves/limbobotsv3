package ru.mrsteve.limbobots;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Хранит всех активных ботов и отвечает за их "поведение":
 * случайное перемещение около спавна и периодические сообщения в чат.
 */
public class BotManager {

    private final LimboBots plugin;
    private final Map<String, Bot> bots = new LinkedHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(1);
    private final Random random = new Random();

    private final List<String> chatMessages = Arrays.asList(
            "привет всем!",
            "как дела на сервере?",
            "го в бой",
            "неплохой сервер, го дружить",
            "кто может помочь с квестом?",
            "лол",
            "ору с этого",
            "го замес",
            "кто в топе сейчас?",
            "интересно тут"
    );

    public BotManager(LimboBots plugin) {
        this.plugin = plugin;
        startMovementTask();
        startChatTask();
    }

    /**
     * Добавляет указанное количество ботов на точку спавна первого мира.
     */
    public int addBots(int amount) {
        Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
        int added = 0;
        for (int i = 0; i < amount; i++) {
            String name = generateName();
            if (bots.containsKey(name)) continue;

            Bot bot = new Bot(name, spawn);
            bot.spawn();
            bots.put(name, bot);
            added++;
        }
        return added;
    }

    /**
     * Удаляет указанное количество ботов (в порядке добавления).
     */
    public int removeBots(int amount) {
        int removed = 0;
        Iterator<Map.Entry<String, Bot>> it = bots.entrySet().iterator();
        while (it.hasNext() && removed < amount) {
            Map.Entry<String, Bot> entry = it.next();
            entry.getValue().remove();
            it.remove();
            removed++;
        }
        return removed;
    }

    /**
     * Удаляет всех ботов.
     */
    public void clearBots() {
        for (Bot bot : bots.values()) {
            bot.remove();
        }
        bots.clear();
    }

    public int getBotCount() {
        return bots.size();
    }

    public Bot getBot(String name) {
        return bots.get(name);
    }

    public Collection<Bot> getAllBots() {
        return bots.values();
    }

    private String generateName() {
        String name;
        do {
            name = "Bot_" + counter.getAndIncrement();
        } while (bots.containsKey(name));
        return name;
    }

    /**
     * Раз в секунду с некоторым шансом двигает каждого бота —
     * так они не двигаются идеально синхронно и выглядят живее.
     */
    private void startMovementTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Bot bot : bots.values()) {
                if (random.nextInt(3) == 0) {
                    bot.moveRandom();
                }
            }
        }, 40L, 20L);
    }

    /**
     * Периодически (в среднем раз в 15-20 секунд) заставляет случайного
     * бота написать сообщение в общий чат сервера.
     */
    private void startChatTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (bots.isEmpty()) return;
            if (random.nextInt(10) != 0) return;

            List<Bot> list = new ArrayList<>(bots.values());
            Bot bot = list.get(random.nextInt(list.size()));
            String message = chatMessages.get(random.nextInt(chatMessages.size()));

            Bukkit.broadcastMessage("§7<§f" + bot.getName() + "§7> §f" + message);
        }, 200L, 100L);
    }
}
