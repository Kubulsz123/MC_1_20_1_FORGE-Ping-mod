package org.zombie_apocalypse.zombie.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.zombie_apocalypse.zombie.ping.ClientPingRenderer;

import java.util.function.Supplier;

public class PingPacket {
    public double x, y, z;

    public PingPacket() {}

    public PingPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(PingPacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.x);
        buf.writeDouble(pkt.y);
        buf.writeDouble(pkt.z);
    }

    public static PingPacket decode(FriendlyByteBuf buf) {
        return new PingPacket(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(PingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            // Sprawdzamy po której stronie jesteśmy
            if (context.getDirection().getReceptionSide().isServer()) {
                // [SERWER] Odbiera ping od gracza i rozsyła do wszystkich w wymiarze
                ServerPlayer sender = context.getSender();
                if (sender != null) {
                    System.out.println("[DEBUG-SERVER] Odebrano pakiet ping. Rozsyłam do innych klientów...");
                    ModNetworking.CHANNEL.send(
                            PacketDistributor.DIMENSION.with(sender.serverLevel()::dimension),
                            new PingPacket(pkt.x, pkt.y, pkt.z)
                    );
                }
            } else {
                // [KLIENT] Odbiera informację od serwera i każe wyrenderować PNG
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("§e[DEBUG] Otrzymano ping z serwera! Uruchamiam renderowanie grafiki."));
                }
                // Dodajemy ping na 100 ticków (5 sekund)
                ClientPingRenderer.addPing(new Vec3(pkt.x, pkt.y, pkt.z), 100);
            }
        });
        context.setPacketHandled(true);
    }
}