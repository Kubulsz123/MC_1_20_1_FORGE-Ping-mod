package org.zombie_apocalypse.zombie.ping;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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

    private static final ResourceLocation PING_TEXTURE = new ResourceLocation("zombie", "textures/gui/ping.png");
    private static final List<PingInstance> ACTIVE_PINGS = new LinkedList<>();

    public static void addPing(Vec3 pos, int ticks) {
        ACTIVE_PINGS.add(new PingInstance(pos, ticks));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator<PingInstance> it = ACTIVE_PINGS.iterator();
        while (it.hasNext()) {
            PingInstance ping = it.next();
            ping.ticksLeft--;
            if (ping.ticksLeft <= 0) it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (ACTIVE_PINGS.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PING_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        Font font = Minecraft.getInstance().font;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (PingInstance ping : ACTIVE_PINGS) {
            double distance = cameraPos.distanceTo(ping.pos);

            poseStack.pushPose();
            poseStack.translate(ping.pos.x, ping.pos.y + 1.2, ping.pos.z);
            poseStack.mulPose(camera.rotation());

            // MATH FOR CONSTANT SCALE: Multiply distance by a base factor.
            // 0.03f makes it roughly 6% smaller than the previous static size at medium range.
            float scale = (float) distance * 0.03f;

            // Prevent it from becoming microscopic if you stand exactly on top of it
            scale = Math.max(0.4f, scale);
            poseStack.scale(-scale, -scale, scale);

            // 1. Draw the PNG Texture
            Matrix4f matrix = poseStack.last().pose();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -1, -1, 0).uv(0, 0).endVertex();
            buffer.vertex(matrix, -1,  1, 0).uv(0, 1).endVertex();
            buffer.vertex(matrix,  1,  1, 0).uv(1, 1).endVertex();
            buffer.vertex(matrix,  1, -1, 0).uv(1, 0).endVertex();
            tesselator.end();

            // 2. Draw the Distance Text
            poseStack.pushPose();
            // Scale the text down relative to the image
            poseStack.scale(0.06f, 0.06f, 0.06f);
            String distText = String.format("%.1f m", distance);
            float textWidth = font.width(distText);

            // Draw text slightly above the icon (Y offset: -25)
            font.drawInBatch(distText, -textWidth / 2.0F, -25.0F, 0xFFFFFF, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            poseStack.popPose();

            poseStack.popPose();
        }

        bufferSource.endBatch(); // Force text render immediately
        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}