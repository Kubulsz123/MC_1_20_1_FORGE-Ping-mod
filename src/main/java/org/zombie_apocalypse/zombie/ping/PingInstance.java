package org.zombie_apocalypse.zombie.ping;

import net.minecraft.world.phys.Vec3;

public class PingInstance {
    public final Vec3 pos;
    public int ticksLeft;

    public PingInstance(Vec3 pos, int ticks) {
        this.pos = pos;
        this.ticksLeft = ticks;
    }
}
