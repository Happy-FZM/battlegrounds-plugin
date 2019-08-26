package com.matsg.battlegrounds.nms.v1_12_R1;

import com.matsg.battlegrounds.api.Version;
import com.matsg.battlegrounds.api.entity.Hellhound;
import com.matsg.battlegrounds.api.entity.Zombie;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.nms.Particle;
import com.matsg.battlegrounds.nms.ReflectionUtils;
import com.matsg.battlegrounds.nms.Title;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Version112R1 implements Version {

    public Hellhound makeHellhound(Game game) {
        return new CustomWolf(game);
    }

    public Zombie makeZombie(Game game) {
        return new CustomZombie(game);
    }

    public void playChestAnimation(Location location, boolean open) {
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        TileEntity tileEntity = world.getTileEntity(blockPosition);

        world.playBlockAction(blockPosition, tileEntity.getBlock(), 1, open ? 1 : 0);
    }

    public void registerCustomEntities() {
        int wolfId = (int) EntityType.WOLF.getTypeId(), zombieId = (int) EntityType.ZOMBIE.getTypeId();
        String wolfKey = "CustomWolf", zombieKey = "CustomZombie";

        EntityTypes.b.a(zombieId, new net.minecraft.server.v1_12_R1.MinecraftKey(zombieKey), CustomZombie.class);
        EntityTypes.b.a(wolfId, new net.minecraft.server.v1_12_R1.MinecraftKey(wolfKey), CustomWolf.class);
    }

    public void sendActionBar(Player player, String message) {
        IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");

        PacketPlayOutChat packet = new PacketPlayOutChat(icbc, ChatMessageType.GAME_INFO);

        ReflectionUtils.sendPacket(player, packet);
    }

    public void sendJSONMessage(Player player, String message, String command, String hoverMessage) {
        String text = "{\"text\":\"\",\"extra\":[{\"text\":\"" + message + "\"," +
                "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + hoverMessage + "\"}," +
                "\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + command + "\"}}]}";

        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(text), ChatMessageType.CHAT);

        ReflectionUtils.sendPacket(player, packet);
    }

    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int time, int fadeOut) {
        new Title(title, subTitle, fadeIn, time, fadeOut).send(player);
    }

    public void spawnColoredParticle(Location location, String effect, float red, float green, float blue) {
        Particle particle = new Particle(Particle.ParticleEffect.valueOf(effect), 0, location, 0, 0, 0, 1);
        particle.setOffsetX(red);
        particle.setOffsetY(green);
        particle.setOffsetZ(blue);
        particle.display();
    }

    public void spawnParticle(Location location, String effect, int amount, float offsetX, float offsetY, float offsetZ, int speed) {
        new Particle(Particle.ParticleEffect.valueOf(effect), amount, location, offsetX, offsetY, offsetZ, speed).display();
    }
}
