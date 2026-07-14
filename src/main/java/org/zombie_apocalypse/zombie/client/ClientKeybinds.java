package org.zombie_apocalypse.zombie.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.zombie_apocalypse.zombie.network.ModNetworking;
import org.zombie_apocalypse.zombie.network.PingPacket;

@Mod.EventBusSubscriber(modid = "zombie", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientKeybinds {
    public static KeyMapping PING_KEY;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        PING_KEY = new KeyMapping("Ping Location", GLFW.GLFW_KEY_P, "Ping System");
        event.register(PING_KEY);
    }

    @Mod.EventBusSubscriber(modid = "zombie", value = Dist.CLIENT)
    public static class KeyEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (PING_KEY != null && PING_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;

                if (player != null) {
                    // Calculate max distance based on user's video settings (render distance in chunks * 16 blocks)
                    double renderDistance = mc.options.renderDistance().get() * 16.0;

                    Vec3 eyePosition = player.getEyePosition();
                    Vec3 lookVector = player.getViewVector(1.0F);
                    Vec3 traceEnd = eyePosition.add(lookVector.x * renderDistance, lookVector.y * renderDistance, lookVector.z * renderDistance);

                    // Perform a custom raycast ignoring fluids, looking only for block outlines
                    ClipContext context = new ClipContext(eyePosition, traceEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                    BlockHitResult hitResult = player.level().clip(context);

                    if (hitResult.getType() == HitResult.Type.BLOCK) {
                        Vec3 pos = hitResult.getLocation();
                        mc.player.sendSystemMessage(Component.literal("§a[DEBUG] Pinged at: " + (int)pos.x + ", " + (int)pos.y + ", " + (int)pos.z));
                        ModNetworking.CHANNEL.sendToServer(new PingPacket(pos.x, pos.y, pos.z));
                    } else {
                        mc.player.sendSystemMessage(Component.literal("§c[DEBUG] Look at a rendered block to ping."));
                    }
                }
            }
        }
    }
}