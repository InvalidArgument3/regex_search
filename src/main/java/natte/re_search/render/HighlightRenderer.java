package natte.re_search.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import natte.re_search.config.Config;
import natte.re_search.search.MarkedInventory;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class HighlightRenderer {

    public static final Minecraft Game = Minecraft.getInstance();

    public static List<RenderedItem> renderedItems = new ArrayList<>();

    public static long renderStartTimestamp = 0;

    private HighlightRenderer() {
    }

    public static void register() {
    }

    public static void startRender() {
        if (Game.level != null) {
            renderStartTimestamp = Game.level.getGameTime();
        }
    }

    public static void onRenderGui(GuiGraphics graphics, float tickDelta) {
        if (!Config.isOldHighlighter) {
            onRenderGuiInner(graphics, tickDelta);
        }
    }

    private static void onRenderGuiInner(GuiGraphics ctx, float tickDelta) {
        if (Game.player == null || Game.level == null) {
            return;
        }
        if (Config.autoHideTime >= 0 && Game.level.getGameTime() > renderStartTimestamp + Config.autoHideTime * 20L) {
            return;
        }

        for (RenderedItem renderedItem : getRenderedItems()) {

            float uiScale = (float) Game.getWindow().getGuiScale();

            if (renderedItem.screenPosition == null) {
                continue;
            }

            var pos = renderedItem.screenPosition;
            var cameraPosVec = Game.player.getEyePosition(tickDelta);
            float distance = (float) cameraPosVec.distanceTo(renderedItem.position);

            if (renderedItem.isArrow) {
                continue;
            }
            BakedModel model = Game.getItemRenderer().getModel(renderedItem.itemStack, Game.level, Game.player, 0);
            float scale = 0.15f / distance;
            scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);
            renderGuiItemModel(ctx, renderedItem.itemStack, pos.x() / uiScale, pos.y() / uiScale, model, scale,
                    renderedItem, distance);
        }
    }

    public static void onRenderWorld(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float tickDelta) {
        processItems(poseStack, projectionMatrix, camera, tickDelta);
    }

    private static void processItems(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float tickDelta) {
        Matrix4f modelViewMatrix = poseStack.last().pose();
        for (RenderedItem renderedItem : getRenderedItems()) {
            renderedItem.screenPosition = project3Dto2D(renderedItem, modelViewMatrix, projectionMatrix, camera);
        }
    }

    public static synchronized List<RenderedItem> getRenderedItems() {
        return renderedItems;
    }

    public static synchronized void setRenderedItems(List<MarkedInventory> inventories) {
        List<RenderedItem> list = new ArrayList<>();
        for (MarkedInventory inventory : WorldRendering.getMarkedInventories()) {
            BlockPos blockPos = inventory.blockPos;
            Vec3 blockPosition = Vec3.atLowerCornerOf(blockPos);
            Vec3 transformedPosition = blockPosition.add(0.5, 0.5, 0.5);
            int size = inventory.inventory.size();
            int sideLength = (int) Math.ceil(Math.sqrt(size));
            int i = -1;
            for (ItemStack itemStack : inventory.inventory) {
                i += 1;
                int x = i % sideLength;
                int y = i / sideLength;
                RenderedItem renderedItem = new RenderedItem();
                renderedItem.itemStack = itemStack;
                renderedItem.position = transformedPosition;
                renderedItem.x = x * 1.125f - (y == (float) size / sideLength ? size % sideLength : sideLength) / 2f
                        + 0.5f - 0.125f * sideLength / 2 + 0.0625f;
                renderedItem.y = y * 1.125f + 1.6f;
                list.add(renderedItem);
            }
            RenderedItem arrow = new RenderedItem();
            arrow.isArrow = true;
            arrow.position = transformedPosition;
            arrow.x = 0;
            arrow.y = 0.8f;
            list.add(arrow);
            i = -1;
            for (ItemStack itemStack : inventory.containers) {
                i += 1;
                int x = i;
                RenderedItem container = new RenderedItem();
                container.itemStack = itemStack;
                container.position = transformedPosition;
                container.x = x * 1.125f - inventory.containers.size() / 2f + 0.5f
                        - 0.125f * inventory.containers.size() / 2f + 0.0625f;
                container.y = 0;
                list.add(container);
            }
        }
        HighlightRenderer.renderedItems = list;
    }

    public static org.joml.Vector4f project3Dto2D(RenderedItem renderedItem, Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix, Camera camera) {
        Vec3 in3d = renderedItem.position.subtract(camera.getPosition());
        var wnd = Game.getWindow();
        var quaternion = new Quaternionf((float) in3d.x, (float) in3d.y, (float) in3d.z, 1.f);
        var product = mqProduct(projectionMatrix, mqProduct(modelViewMatrix, quaternion));
        if (product.w <= 0f) {
            return null;
        }
        var screenPos = qToScreen(product);
        float x = screenPos.x * wnd.getWidth();
        float y = screenPos.y * wnd.getHeight();
        if (Float.isInfinite(x) || Float.isInfinite(y)) {
            return null;
        }
        return new org.joml.Vector4f(x, wnd.getHeight() - y, screenPos.z, 1f / (screenPos.w * 2f));
    }

    private static Quaternionf mqProduct(Matrix4f m, Quaternionf q) {
        return new Quaternionf(
                m.m00() * q.x + m.m10() * q.y + m.m20() * q.z + m.m30() * q.w,
                m.m01() * q.x + m.m11() * q.y + m.m21() * q.z + m.m31() * q.w,
                m.m02() * q.x + m.m12() * q.y + m.m22() * q.z + m.m32() * q.w,
                m.m03() * q.x + m.m13() * q.y + m.m23() * q.z + m.m33() * q.w);
    }

    private static Quaternionf qToScreen(Quaternionf q) {
        float w = 1f / q.w * 0.5f;
        return new Quaternionf(q.x * w + 0.5f, q.y * w + 0.5f, q.z * w + 0.5f, w);
    }

    private static void renderGuiItemModel(GuiGraphics graphics, ItemStack itemStack, double x, double y,
            BakedModel model, float scale, RenderedItem renderedItem, float distance) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 100);
        float c = 1000f;
        pose.translate(-renderedItem.x * scale * c, -renderedItem.y * scale * c,
                -distance * distance);
        pose.scale(scale, scale, scale);
        pose.scale(1f, -1f, 1f);
        pose.scale(1000f, 1000f, 1000f);
        MultiBufferSource.BufferSource buffer = Game.renderBuffers().bufferSource();
        Game.getItemRenderer().render(itemStack, ItemDisplayContext.GUI, false, pose, buffer, 15728880,
                OverlayTexture.NO_OVERLAY, model);
        buffer.endBatch();
        pose.popPose();
    }
}
