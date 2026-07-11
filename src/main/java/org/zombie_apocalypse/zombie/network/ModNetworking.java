package org.zombie_apocalypse.zombie.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional; // <-- Upewnij się, że masz ten import

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("zombie", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, PingPacket.class,
                PingPacket::encode,
                PingPacket::decode,
                PingPacket::handle,
                Optional.empty() // Zmienione z Optional.of(...) – teraz paczka działa w obie strony (C2S oraz S2C)
        );
    }
}