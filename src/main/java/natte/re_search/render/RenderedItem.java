package natte.re_search.render;

import org.joml.Vector4f;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class RenderedItem {
    public Vec3 position;
    public Vector4f screenPosition;

    public float x;
    public float y;

    public ItemStack itemStack;

    public boolean isArrow = false;

    public RenderedItem() {
    }
}
