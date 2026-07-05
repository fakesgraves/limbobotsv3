package ru.mrsteve.limbobots;

import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LimboBotsCommand implements CommandExecutor, TabCompleter {

    private final LimboBots plugin;

    public LimboBotsCommand(LimboBots plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("limbobots.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                handleAdd(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "clear":
                plugin.getBotManager().clearBots();
                sender.sendMessage(ChatColor.GREEN + "Все боты удалены.");
                break;
            case "tp":
                handleTeleport(sender, args);
                break;
            case "equip":
                handleEquip(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            default:
                sendHelp(sender);
        }
        return true;
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /limbobots add <количество>");
            return;
        }
        int amount = parseInt(sender, args[1]);
        if (amount <= 0) return;

        int added = plugin.getBotManager().addBots(amount);
        sender.sendMessage(ChatColor.GREEN + "Добавлено ботов: " + added +
                ChatColor.GRAY + " (всего сейчас: " + plugin.getBotManager().getBotCount() + ")");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /limbobots remove <количество>");
            return;
        }
        int amount = parseInt(sender, args[1]);
        if (amount <= 0) return;

        int removed = plugin.getBotManager().removeBots(amount);
        sender.sendMessage(ChatColor.GREEN + "Удалено ботов: " + removed +
                ChatColor.GRAY + " (осталось: " + plugin.getBotManager().getBotCount() + ")");
    }

    private void handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду может использовать только игрок.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /limbobots tp <имя_бота>");
            return;
        }
        Bot bot = plugin.getBotManager().getBot(args[1]);
        if (bot == null) {
            sender.sendMessage(ChatColor.RED + "Бот с именем '" + args[1] + "' не найден.");
            return;
        }
        ((Player) sender).teleport(bot.getLocation());
        sender.sendMessage(ChatColor.GREEN + "Вы телепортированы к боту " + bot.getName());
    }

    private void handleEquip(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Использование: /limbobots equip <имя_бота> <слот> <предмет>");
            sender.sendMessage(ChatColor.GRAY + "Слоты: head, chest, legs, feet, hand, offhand");
            return;
        }
        Bot bot = plugin.getBotManager().getBot(args[1]);
        if (bot == null) {
            sender.sendMessage(ChatColor.RED + "Бот с именем '" + args[1] + "' не найден.");
            return;
        }

        Material material;
        try {
            material = Material.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Неизвестный предмет: " + args[3]);
            return;
        }

        EnumItemSlot slot = parseSlot(args[2]);
        if (slot == null) {
            sender.sendMessage(ChatColor.RED + "Неизвестный слот: " + args[2] +
                    ChatColor.GRAY + " (head, chest, legs, feet, hand, offhand)");
            return;
        }

        bot.equip(slot, new ItemStack(material));
        sender.sendMessage(ChatColor.GREEN + "Предмет " + material + " надет на бота " + bot.getName());
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Активные боты (" + plugin.getBotManager().getBotCount() + "):");
        for (Bot bot : plugin.getBotManager().getAllBots()) {
            sender.sendMessage(ChatColor.YELLOW + " - " + bot.getName());
        }
    }

    private EnumItemSlot parseSlot(String input) {
        switch (input.toLowerCase()) {
            case "head": return EnumItemSlot.HEAD;
            case "chest": return EnumItemSlot.CHEST;
            case "legs": return EnumItemSlot.LEGS;
            case "feet": return EnumItemSlot.FEET;
            case "hand": return EnumItemSlot.MAINHAND;
            case "offhand": return EnumItemSlot.OFFHAND;
            default: return null;
        }
    }

    private int parseInt(CommandSender sender, String raw) {
        try {
            int value = Integer.parseInt(raw);
            if (value <= 0) {
                sender.sendMessage(ChatColor.RED + "Количество должно быть больше нуля.");
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "'" + raw + "' не является числом.");
            return -1;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== LimboBots (автор: MrSteve) =====");
        sender.sendMessage(ChatColor.YELLOW + "/limbobots add <количество>" + ChatColor.GRAY + " - добавить ботов");
        sender.sendMessage(ChatColor.YELLOW + "/limbobots remove <количество>" + ChatColor.GRAY + " - удалить N ботов");
        sender.sendMessage(ChatColor.YELLOW + "/limbobots clear" + ChatColor.GRAY + " - удалить всех ботов");
        sender.sendMessage(ChatColor.YELLOW + "/limbobots list" + ChatColor.GRAY + " - список ботов");
        sender.sendMessage(ChatColor.YELLOW + "/limbobots tp <имя>" + ChatColor.GRAY + " - телепортироваться к боту");
        sender.sendMessage(ChatColor.YELLOW + "/limbobots equip <имя> <слот> <предмет>" + ChatColor.GRAY + " - надеть предмет");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("add", "remove", "clear", "tp", "equip", "list"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("equip"))) {
            for (Bot bot : plugin.getBotManager().getAllBots()) {
                completions.add(bot.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("equip")) {
            completions.addAll(Arrays.asList("head", "chest", "legs", "feet", "hand", "offhand"));
        }

        String current = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> filtered = new ArrayList<>();
        for (String c : completions) {
            if (c.toLowerCase().startsWith(current)) {
                filtered.add(c);
            }
        }
        return filtered;
    }
}
