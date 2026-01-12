package org.zombie_apocalypse.zombie.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.zombie_apocalypse.zombie.ping.PingInstance;
import org.zombie_apocalypse.zombie.ping.PingManager;

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
        return new PingPacket(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static void handle(PingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();

            PingManager.addPing(
                    level,
                    new PingInstance(
                            new Vec3(pkt.x, pkt.y, pkt.z),
                            200
                    )
            );
        });
        ctx.get().setPacketHandled(true);
    }

}
