package io.github.srdjanv.forkedproxy.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockAccessProxy extends ItemBlock {
    public ItemBlockAccessProxy(Block block) {
        super(block);
        this.setMaxStackSize(64);
    }
}
