package natte.re_search.render;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import natte.re_search.config.Config;
import natte.re_search.search.MarkedInventory;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class WorldRendering {

    public static List<MarkedInventory> inventories = new ArrayList<>();

    private WorldRendering() {
    }

    public static void register() {
    }

    public static void renderOldHighlighter(RenderLevelStageEvent event) {
        if (!Config.isOldHighlighter) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        if (getMarkedInventoriesSize() == 0) {
            return;
        }
        if (client.level.getGameTime() > HighlightRenderer.renderStartTimestamp + Config.autoHideTime * 20L) {
            return;
        }
        Camera camera = event.getCamera();
        ItemRenderer itemRenderer = client.getItemRenderer();
        MultiBufferSource.BufferSource vertexConsumers = client.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();

        for (MarkedInventory inventory : getMarkedInventories()) {
            BlockPos blockPos = inventory.blockPos;
            Vec3 relativeBlockPosition = Vec3.atLowerCornerOf(blockPos).subtract(camera.getPosition());

            int size = inventory.inventory.size();
            int sideLength = (int) Math.ceil(Math.sqrt(size));
            int packedLight = LevelRenderer.getLightColor(client.level, blockPos);

            poseStack.pushPose();
            {
                Vec3 transformedPosition = relativeBlockPosition.add(0.5, 0.5, 0.5);
                float distance = (float) transformedPosition.length();
                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
                poseStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                poseStack.mulPose(Axis.YP.rotation((float) Mth.atan2(-transformedPosition.x, -transformedPosition.z)));
                poseStack.mulPose(Axis.XP.rotation((float) Mth.atan2(transformedPosition.y,
                        Math.hypot(transformedPosition.x, transformedPosition.z))));
                poseStack.translate(0, 0, distance - 0.25f);
                poseStack.scale(scale, scale, scale);

                int i = -1;
                for (ItemStack itemStack : inventory.inventory) {
                    i += 1;
                    int x = i % sideLength;
                    int y = i / sideLength;
                    poseStack.pushPose();
                    poseStack.translate(
                            x * 1.125f - (y == (float) size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f
                                    - 0.125f * sideLength / 2 + 0.0625,
                            y * 1.125f + 1.6f, 0f);
                    itemRenderer.renderStatic(itemStack, ItemDisplayContext.GUI, packedLight, OverlayTexture.NO_OVERLAY,
                            poseStack, vertexConsumers, client.level, 0);
                    poseStack.popPose();
                }
            }
            poseStack.popPose();

            poseStack.pushPose();
            {
                poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
                Vec3 transformedPosition = relativeBlockPosition.add(0.5, 0.5, 0.5);
                poseStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                poseStack.mulPose(Axis.YP.rotation((float) Mth.atan2(-transformedPosition.x, -transformedPosition.z)));
                poseStack.mulPose(Axis.XP.rotation((float) Mth.atan2(transformedPosition.y,
                        Math.hypot(transformedPosition.x, transformedPosition.z))));
                float distance = (float) transformedPosition.length();
                poseStack.translate(0, 0, distance - 0.25f);
                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);
                poseStack.scale(scale, scale, scale);
                int j = -1;
                for (ItemStack itemStack : inventory.containers) {
                    j += 1;
                    int x = j;
                    poseStack.pushPose();
                    poseStack.translate(x * 1.125f - inventory.containers.size() / 2f + 0.5f
                            - 0.125f * inventory.containers.size() / 2 + 0.0625, 0f, 0f);
                    itemRenderer.renderStatic(itemStack, ItemDisplayContext.GUI, packedLight, OverlayTexture.NO_OVERLAY,
                            poseStack, vertexConsumers, client.level, 0);
                    poseStack.popPose();
                }
            }
            poseStack.popPose();
        }

        vertexConsumers.endBatch();
    }

    public static synchronized void addMarkedInventory(MarkedInventory inventory) {
        inventories.add(inventory);
    }

    public static synchronized void clearMarkedInventories() {
        inventories.clear();
    }

    public static synchronized int getMarkedInventoriesSize() {
        return inventories.size();
    }

    static synchronized List<MarkedInventory> getMarkedInventories() {
        return inventories;
    }

    public static synchronized void setMarkedInventories(List<MarkedInventory> _inventories) {
        inventories = _inventories;
        HighlightRenderer.setRenderedItems(_inventories);
    }
}
