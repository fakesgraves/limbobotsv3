package ru.mrsteve.limbobots;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Обёртка над NMS EntityPlayer — представляет одного "живого" бота.
 * Бот не является реальным подключением, а полностью управляется
 * через пакеты, отправляемые настоящим игрокам.
 */
public class Bot {

    private final String name;
    private final UUID uuid;
    private final Random random = new Random();

    private EntityPlayer npc;
    private Location location;
    private float yaw;

    public Bot(String name, Location spawnLocation) {
        this.name = name;
        this.uuid = UUID.randomUUID();
        this.location = spawnLocation.clone();
        this.yaw = spawnLocation.getYaw();
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location.clone();
    }

    public EntityPlayer getHandle() {
        return npc;
    }

    /**
     * Создаёт NMS-сущность игрока и показывает её всем онлайн-игрокам.
     */
    public void spawn() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();

        GameProfile profile = new GameProfile(uuid, name);
        PlayerInteractManager interactManager = new PlayerInteractManager(worldServer);

        npc = new EntityPlayer(server, worldServer, profile, interactManager);
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        for (Player player : Bukkit.getOnlinePlayers()) {
            sendSpawnPackets(player);
        }
    }

    /**
     * Отправляет пакеты появления бота конкретному игроку
     * (используется и при создании бота, и когда на сервер заходит новый игрок).
     */
    public void sendSpawnPackets(Player player) {
        if (npc == null) return;
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        // Добавляем в таб-лист
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
        // Спауним модель в мире
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (yaw * 256 / 360)));
        connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), true));
    }

    /**
     * Полностью убирает бота у конкретного игрока (таб + модель).
     */
    public void sendRemovePackets(Player player) {
        if (npc == null) return;
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }

    /**
     * Убирает бота у всех игроков на сервере.
     */
    public void remove() {
        if (npc == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendRemovePackets(player);
        }
    }

    /**
     * Надевает предмет в указанный слот экипировки (визуально, через пакет).
     */
    public void equip(EnumItemSlot slot, org.bukkit.inventory.ItemStack item) {
        if (npc == null) return;

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipmentList = new ArrayList<>();
        equipmentList.add(new Pair<>(slot, nmsItem));

        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(npc.getId(), equipmentList);
        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    /**
     * Двигает бота на случайную небольшую дистанцию рядом с текущей точкой
     * и рассылает всем игрокам пакет телепортации + поворота головы.
     */
    public void moveRandom() {
        if (npc == null || location.getWorld() == null) return;

        double dx = (random.nextDouble() - 0.5) * 3;
        double dz = (random.nextDouble() - 0.5) * 3;

        Location target = location.clone().add(dx, 0, dz);

        // Простая проверка, чтобы бот не улетал в воздух/под землю:
        // ищем безопасную Y на пару блоков выше/ниже текущей точки.
        target.setY(findSafeY(target));

        this.yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        this.location = target;

        npc.setLocation(target.getX(), target.getY(), target.getZ(), yaw, 0);

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(npc);
        PacketPlayOutEntityHeadRotation headPacket = new PacketPlayOutEntityHeadRotation(npc, (byte) (yaw * 256 / 360));

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(teleportPacket);
            connection.sendPacket(headPacket);
        }
    }

    private double findSafeY(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int startY = loc.getWorld().getHighestBlockYAt(x, z);
        return startY + 1;
    }
}
