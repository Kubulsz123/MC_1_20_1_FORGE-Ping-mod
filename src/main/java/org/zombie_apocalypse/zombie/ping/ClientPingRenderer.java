package org.zombie_apocalypse.zombie.ping;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientPingRenderer {

    // Ścieżka do Twojego pliku PNG. Za chwilę napiszę, gdzie go wrzucić.
    private static final ResourceLocation PING_TEXTURE = new ResourceLocation("zombie", "textures/gui/ping.png");
    private static final List<PingInstance> ACTIVE_PINGS = new LinkedList<>();

    public static void addPing(Vec3 pos, int ticks) {
        ACTIVE_PINGS.add(new PingInstance(pos, ticks));
    }

    // Licznik czasu (usuwanie po X sekundach)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<PingInstance> it = ACTIVE_PINGS.iterator();
        while (it.hasNext()) {
            PingInstance ping = it.next();
            ping.ticksLeft--;
            if (ping.ticksLeft <= 0) {
                it.remove();
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[DEBUG] Czas pingu minął, znika z ekranu."));
                }
            }
        }
    }

    // Właściwe rysowanie PNG w 3D
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        // Rysujemy na etapie nakładania przezroczystych elementów
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (ACTIVE_PINGS.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        // Ustawienia renderera pod 2D PNG
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PING_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false); // Żeby ping przebijał lekko przez inne rzeczy

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (PingInstance ping : ACTIVE_PINGS) {
            poseStack.pushPose();

            // 1. Przesuwamy w miejsce trafienia, lekko do góry (y + 1.2), żeby nie siedział w podłodze
            poseStack.translate(ping.pos.x, ping.pos.y + 1.2, ping.pos.z);

            // 2. Obracamy przodem do gracza (billboarding)
            poseStack.mulPose(camera.rotation());

            // 3. Skalowanie rozmiaru grafiki
            poseStack.scale(-0.8F, -0.8F, 0.8F);

            // 4. Budowanie kwadratu z naniesioną teksturą (UV od 0 do 1)
            Matrix4f matrix = poseStack.last().pose();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -1, -1, 0).uv(0, 0).endVertex(); // Dolny lewy
            buffer.vertex(matrix, -1,  1, 0).uv(0, 1).endVertex(); // Górny lewy
            buffer.vertex(matrix,  1,  1, 0).uv(1, 1).endVertex(); // Górny prawy
            buffer.vertex(matrix,  1, -1, 0).uv(1, 0).endVertex(); // Dolny prawy
            tesselator.end();

            poseStack.popPose();
        }

        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}