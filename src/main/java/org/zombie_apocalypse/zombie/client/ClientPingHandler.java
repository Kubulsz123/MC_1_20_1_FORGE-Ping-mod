package org.zombie_apocalypse.zombie.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

public class ClientPingHandler {

    public static void spawnPing(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        for (int i = 0; i < 20; i++) {
            level.addParticle(
                    ParticleTypes.GLOW,
                    pos.getX() + 0.5,
                    pos.getY() + 1.2,
                    pos.getZ() + 0.5,
                    0,
                    0.05,
                    0
            );
        }
    }
}
