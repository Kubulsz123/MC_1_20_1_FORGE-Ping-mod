package org.zombie_apocalypse.zombie.ping;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber
public class PingManager {

    private static final List<PingInstance> PINGS = new LinkedList<>();

    public static void addPing(ServerLevel level, PingInstance ping) {
        PINGS.add(ping);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<PingInstance> it = PINGS.iterator();

        while (it.hasNext()) {
            PingInstance ping = it.next();
            ping.ticksLeft--;

            ServerLevel level = event.getServer().overworld();

            double x = ping.pos.x;
            double y = ping.pos.y;
            double z = ping.pos.z;

            // Ping Marker
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x, y + 0.1, z,
                    6, 0.3, 0.1, 0.3, 0
            );

            System.out.println("[PING] Particle spawn OK");


            // 🔸 Ping beam
            for (int i = 0; i < 120; i++) {
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        x, y + i * 0.5, z,
                        1, 0, 0, 0, 0
                );
            }

            System.out.println("[PING] Particle beam OK");



            if (ping.ticksLeft <= 0) {
                it.remove();
            }
        }
    }
}
