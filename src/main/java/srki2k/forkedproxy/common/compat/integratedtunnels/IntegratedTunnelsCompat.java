package srki2k.forkedproxy.common.compat.integratedtunnels;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integratedtunnels.api.world.IBlockBreakHandler;
import org.cyclops.integratedtunnels.api.world.IBlockBreakHandlerRegistry;
import srki2k.forkedproxy.common.block.BlockAccessProxy;

public class IntegratedTunnelsCompat {
    public static void init() {
        IBlockBreakHandlerRegistry REGISTRY = org.cyclops.integratedtunnels.IntegratedTunnels._instance.getRegistryManager().getRegistry(IBlockBreakHandlerRegistry.class);
        REGISTRY.register(BlockAccessProxy.getInstance(), new BlockBreakHandlerProxy());
    }

    static class BlockBreakHandlerProxy implements IBlockBreakHandler {

        @Override
        public boolean shouldApply(IBlockState blockState, World world, BlockPos pos, EntityPlayer player) {
            return true;
        }

        @Override
        public NonNullList<ItemStack> getDrops(IBlockState blockState, World world, BlockPos pos, EntityPlayer player) {
            return null;
        }

        @Override
        public void breakBlock(IBlockState blockState, World world, BlockPos pos, EntityPlayer player) {
        }
    }

}
