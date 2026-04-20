package net.natte.regex_search.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.natte.regex_search.RegexSearch;
import net.natte.regex_search.client.ClientSettings;
import net.natte.regex_search.search.MarkedInventory;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;


public class HighlightRenderer {
    private static final ResourceLocation ARROW_TEXTURE = RegexSearch.ID("textures/arrow.png");
    private static List<MarkedInventory> inventories = new ArrayList<>();

    public static synchronized void clear() {
        inventories = List.of();
        renderedItems.clear();
    }

    public static synchronized void setMarkedInventories(List<MarkedInventory> _inventories) {
        inventories = _inventories;
        HighlightRenderer.updateRenderedItems();
    }

    public static List<RenderedItem> renderedItems = List.of();

    public static long renderStartTimestamp = 0;

    private static Minecraft client;

    public static void startRender() {
        renderStartTimestamp = client.level.getGameTime();
    }

    public static void onRenderGUI(PoseStack poseStack, float tickDelta) {
        client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        if (ClientSettings.autoHideTime >= 0 && client.level.getGameTime() > renderStartTimestamp + ClientSettings.autoHideTime * 20)
            return;

        for (RenderedItem renderedItem : getRenderedItems()) {

            var uiScale = (float) client.getWindow().getGuiScale();

            if (renderedItem.screenPosition == null)
                continue;


            var pos = renderedItem.screenPosition;
            var cameraPosVec = client.gameRenderer.getMainCamera().getPosition();
            var distance = (float) cameraPosVec.distanceTo(renderedItem.position);

            poseStack.pushPose();

            if (renderedItem.isArrow) {

                poseStack.pushPose();
                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                float sc = scale * 1000f;
                poseStack.translate(pos.x / uiScale, pos.y / uiScale, 0);
                poseStack.translate(-renderedItem.x * sc, -renderedItem.y * sc, sc);
                poseStack.scale(scale, scale, 1);
                poseStack.scale(1.f, -1.f, 1.f);
                float sc2 = 1000f;
                poseStack.scale(sc2, sc2, 1);

                Matrix4f positionMatrix = poseStack.last().pose();

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

                buffer.addVertex(positionMatrix, -0.5f, 0.5f, 0).setUv(0f, 0f);
                buffer.addVertex(positionMatrix, -0.5f, -0.5f, 0).setUv(0f, 1f);
                buffer.addVertex(positionMatrix, 0.5f, -0.5f, 0).setUv(1f, 1f);
                buffer.addVertex(positionMatrix, 0.5f, 0.5f, 0).setUv(1f, 0f);

                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, ARROW_TEXTURE);
                RenderSystem.disableCull();

                BufferUploader.drawWithShader(buffer.buildOrThrow());

                RenderSystem.enableCull();

                poseStack.popPose();

            } else {
                var model = client.getItemRenderer().getModel(renderedItem.itemStack, null, null, 0);

                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                renderGuiItemModel(
                        renderedItem.itemStack,
                        (pos.x / uiScale),
                        (pos.y / uiScale),
                        model,
                        scale, renderedItem);
            }
            poseStack.popPose();
        }
    }

    public static void onRenderWorld(PoseStack matrixStack, Matrix4f projectionMatrix, Camera camera, DeltaTracker deltaTracker) {
        float tickDelta = deltaTracker.getGameTimeDeltaTicks();
        processItems(matrixStack, projectionMatrix, camera, tickDelta);


    }

    private static void processItems(PoseStack matrixStack, Matrix4f projectionMatrix, Camera camera, float tickDelta) {
        Matrix4f modelViewMatrix = matrixStack.last().pose();
        for (RenderedItem renderedItem : getRenderedItems()) {
            renderedItem.screenPosition = project3Dto2D(renderedItem, modelViewMatrix, projectionMatrix, camera);
        }
    }

    private static synchronized List<RenderedItem> getRenderedItems() {
        return renderedItems;
    }

    public static synchronized void updateRenderedItems() {

        List<RenderedItem> renderedItems = new ArrayList<>();
        for (MarkedInventory inventory : inventories) {
            BlockPos blockPos = inventory.blockPos();

            Vec3 blockPosition = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            Vec3 transformedPosition = blockPosition.add(0.5, 0.5, 0.5);

            int size = inventory.inventory().size();
            int sideLength = (int) Math.ceil(Math.sqrt(size));

            int i = -1;
            for (ItemStack itemStack : inventory.inventory()) {
                i += 1;
                int x = i % sideLength;
                int y = i / sideLength;

                RenderedItem renderedItem = new RenderedItem();
                renderedItem.itemStack = itemStack;

                renderedItem.position = transformedPosition;
                renderedItem.x = x * 1.125f - (y == (float) size / sideLength ? size % sideLength : sideLength) / 2f
                        + 0.5f - 0.125f * sideLength / 2 + 0.0625f;
                renderedItem.y = y * 1.125f + 1.6f;

                renderedItems.add(renderedItem);
            }

            {
                RenderedItem arrow = new RenderedItem();
                arrow.isArrow = true;
                arrow.position = transformedPosition;
                arrow.x = 0;
                arrow.y = 0.8f;
                renderedItems.add(arrow);
            }
            i = -1;
            for (ItemStack itemStack : inventory.containers()) {
                i += 1;
                int x = i;

                RenderedItem container = new RenderedItem();
                container.itemStack = itemStack;
                container.position = transformedPosition;
                container.x = x * 1.125f - inventory.containers().size() / 2f + 0.5f
                        - 0.125f * inventory.containers().size() / 2f + 0.0625f;
                container.y = 0;
                renderedItems.add(container);
            }

        }
        HighlightRenderer.renderedItems = renderedItems;
    }

    public static Vector4f project3Dto2D(RenderedItem renderedItem, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Camera camera) {
        var wnd = Minecraft.getInstance().getWindow();
        Vec3 in3d = renderedItem.position.subtract(camera.getPosition());
        Quaternionf quaternion = new Quaternionf((float) in3d.x, (float) in3d.y, (float) in3d.z, 1f);
        Matrix4f m = new Matrix4f(modelViewMatrix);
        m.rotate(new Quaternionf(camera.rotation()).invert());
        Quaternionf product = mqProduct(projectionMatrix, mqProduct(m, quaternion));

        if (product.w <= 0f) {
            return null;
        }

        Quaternionf screenPos = qToScreen(product);
        float x = screenPos.x * wnd.getWidth();
        float y = screenPos.y * wnd.getHeight();

        if (Float.isInfinite(x) || Float.isInfinite(y)) {
            return null;
        }

        return new Vector4f(x, wnd.getHeight() - y, screenPos.z, 1f / (screenPos.w * 2f));
    }

    public static Vec2 project3Dto2D(Vec3 worldPosition, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Camera camera) {
        var wnd = Minecraft.getInstance().getWindow();
        Vec3 in3d = worldPosition.subtract(camera.getPosition());
        Quaternionf quaternion = new Quaternionf((float) in3d.x, (float) in3d.y, (float) in3d.z, 1f);
        Matrix4f m = new Matrix4f(modelViewMatrix);
        m.rotate(new Quaternionf(camera.rotation()).invert());
        Quaternionf product = mqProduct(projectionMatrix, mqProduct(m, quaternion));

        if (product.w <= 0f) {
            return null;
        }

        Quaternionf screenPos = qToScreen(product);
        float x = screenPos.x * wnd.getWidth();
        float y = screenPos.y * wnd.getHeight();

        if (Float.isInfinite(x) || Float.isInfinite(y)) {
            return null;
        }

        return new Vec2(x, wnd.getHeight() - y);
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

    public static void renderGuiItemModel(ItemStack itemStack, double x, double y, BakedModel model, float scale, RenderedItem renderedItem) {
        var matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        matrixStack.translate((float) x, (float) y, 0);

        float sc = scale * 1000f;
        matrixStack.translate(-renderedItem.x * sc, -renderedItem.y * sc, sc);
        matrixStack.scale(sc, sc, 1);
        RenderSystem.applyModelViewMatrix();

        PoseStack itemPoseStack = new PoseStack();
        itemPoseStack.scale(1, -1, 1);
        var immediate = client.renderBuffers().bufferSource();

        var bl = !model.usesBlockLight();
        if (bl) {
            Lighting.setupForFlatItems();
        }
        client.getItemRenderer().render(
                itemStack,
                ItemDisplayContext.GUI,
                false,
                itemPoseStack,
                immediate,
                0xf000f0,
                OverlayTexture.NO_OVERLAY,
                model);

        immediate.endBatch();
        RenderSystem.enableDepthTest();
        if (bl) {
            Lighting.setupFor3DItems();
        }

        matrixStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
}
