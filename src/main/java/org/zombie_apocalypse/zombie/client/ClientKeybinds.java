package org.zombie_apocalypse.zombie.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
        PING_KEY = new KeyMapping(
                "key.zombie.ping",       // translation key
                GLFW.GLFW_KEY_P,         // default key
                "key.categories.zombie"  // category
        );
        event.register(PING_KEY);      // <-- KEYBIND REGISTER
    }

    @Mod.EventBusSubscriber(modid = "zombie", value = Dist.CLIENT)
    public static class KeyEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (PING_KEY != null && PING_KEY.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                mc.player.sendSystemMessage(Component.literal("PING_KEY pressed!"));

                // package sender to the server
                ModNetworking.CHANNEL.sendToServer(
                        new PingPacket(
                                mc.player.getX(),
                                mc.player.getY(),
                                mc.player.getZ()
                        )
                );
            }

        }
    }
}
