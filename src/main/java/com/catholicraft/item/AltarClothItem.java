package com.catholicraft.item;

import net.minecraft.world.item.Item;

/** The color index is shared by altar blockstates, models, and crafting. */
public final class AltarClothItem extends Item {
    private final int color;
    public AltarClothItem(Properties p, int color) { super(p); this.color = color; }
    public int color() { return color; }
}
