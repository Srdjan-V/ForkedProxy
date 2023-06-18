package io.github.srdjanv.forkedproxy.common.block;

import io.github.srdjanv.forkedproxy.common.container.ContainerAccessProxy;
import io.github.srdjanv.forkedproxy.common.tileentity.TileAccessProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import io.github.srdjanv.forkedproxy.client.gui.GuiAccessProxy;

public class BlockAccessProxy extends BlockContainerGuiCabled {

    private static BlockAccessProxy _instance;

    public static BlockAccessProxy getInstance() {
        return _instance;
    }

    public BlockAccessProxy(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, TileAccessProxy.class);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerAccessProxy.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiAccessProxy.class;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);

        //why was this even needed?
  /*      if (!world.isRemote) {
            TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
            if (te == null) {
                return;
            }
            for (EnumFacing facing : EnumFacing.values()) {
                if (te.target != null && world.isBlockLoaded(te.target.getBlockPos()) && !te.isInvalid()) {
                    IDynamicRedstone cap = TileHelpers.getCapability(DimPos.of(world, pos.offset(facing)), facing.getOpposite(), DynamicRedstoneConfig.CAPABILITY);
                    te.setSideRedstonePower(facing, cap);
                }
            }
        }*/
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        if (!world.isRemote) {
            TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
            if (te == null) {
                return;
            }
            te.sendRemoveRenderPacket();
            te.unRegisterProxyFromWorld();
            te.updateTargetBlock();
        }
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    protected void onPostBlockDestroyed(World world, BlockPos pos) {
        super.onPostBlockDestroyed(world, pos);
        TileAccessProxy.updateAfterBlockDestroy(world, pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            if (player.isSneaking()) {
                if (player.getHeldItem(hand).isEmpty()) {
                    ((TileAccessProxy) world.getTileEntity(pos)).changeDisableRender();
                    return true;
                }
            } else if (WrenchHelpers.isWrench(player, player.getHeldItem(hand), world, pos, side)) {
                ((TileAccessProxy) world.getTileEntity(pos)).rotateDisplayValue(side);
                return true;
            }

            return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        }

        //if (world.isRemote)
        if (!player.isSneaking()) {
            if (WrenchHelpers.isWrench(player, player.getHeldItem(hand), world, pos, side)) {
                return true;
            } else {
                return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
            }
        }

        return player.getHeldItem(hand).isEmpty();

    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos);
        if (!world.isRemote) {
            if (neighborBlock instanceof BlockAccessProxy) {
                return;
            }

            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileAccessProxy) {
                Vec3i facing_vec = fromPos.subtract(new Vec3i(pos.getX(), pos.getY(), pos.getZ()));
                EnumFacing facing = EnumFacing.getFacingFromVector(facing_vec.getX(), facing_vec.getY(), facing_vec.getZ());

                TileAccessProxy tileAccessProxy = (TileAccessProxy) tileEntity;
                IDynamicRedstone cap = TileHelpers.getCapability(DimPos.of(world, fromPos), facing.getOpposite(), DynamicRedstoneConfig.CAPABILITY);
                if (tileAccessProxy.setSideRedstonePower(facing, cap)) {
                    tileAccessProxy.updateTargetBlock();
                }
            }
        }
    }

    @Override
    public boolean isKeepNBTOnDrop() {
        return false;
    }

}
