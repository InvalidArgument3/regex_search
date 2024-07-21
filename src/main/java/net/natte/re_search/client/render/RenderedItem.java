package net.natte.re_search.client.render;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public class RenderedItem {
    public Vec3 position;
    public Vector4f screenPosition;

    public float x;
    public float y;

    public ItemStack itemStack;

    public boolean isArrow = false;
}
