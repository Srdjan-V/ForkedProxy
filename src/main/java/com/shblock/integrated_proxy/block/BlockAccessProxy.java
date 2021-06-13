package com.shblock.integrated_proxy.block;

import com.shblock.integrated_proxy.IntegratedProxy;
import com.shblock.integrated_proxy.client.gui.GuiAccessProxy;
import com.shblock.integrated_proxy.inventory.container.ContainerAccessProxy;
import com.shblock.integrated_proxy.storage.AccessProxyCollection;
import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.libraries.ModList;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integratedtunnels.core.ExtendedFakePlayer;

@Mod.EventBusSubscriber(modid = IntegratedProxy.MODID)
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
        if (!world.isRemote) {
            AccessProxyCollection.getInstance(world).set(pos, pos);
            TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
            if (te == null) {
                return;
            }
            te.target = DimPos.of(world, pos);
        }
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        if (!world.isRemote) {
            TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
            if (te == null) {
                return;
            }
            te.sendRemoveRenderPacket();
            te.unRegisterEventHandle();
            AccessProxyCollection.getInstance(world).remove(pos);
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
                return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
            } else {
                if (WrenchHelpers.isWrench(player, player.getHeldItem(hand), world, pos, side)) {
                    ((TileAccessProxy) world.getTileEntity(pos)).rotateDisplayValue(side);
                    return true;
                } else {
                    return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
                }
            }
        } else {
            if (!player.isSneaking()) {
                if (WrenchHelpers.isWrench(player, player.getHeldItem(hand), world, pos, side)) {
                    return true;
                } else {
                    return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
                }
            } else {
                if (player.getHeldItem(hand).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos);
        if (neighborBlock instanceof BlockAccessProxy) {
            return;
        }
        if (pos.getY() == fromPos.getY() && !world.isRemote) {
            Vec3i facing_vec = fromPos.subtract(new Vec3i(pos.getX(), pos.getY(), pos.getZ()));
            EnumFacing facing = EnumFacing.getFacingFromVector(facing_vec.getX(), facing_vec.getY(), facing_vec.getZ());
            TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
            if (te != null) {
                IDynamicRedstone cap = TileHelpers.getCapability(DimPos.of(world, fromPos), facing.getOpposite(), DynamicRedstoneConfig.CAPABILITY);
                if (te.setSideRedstonePower(facing, cap)) {
                    te.updateTargetBlock();
                }
            }
        }
    }

    @Override
    public boolean isKeepNBTOnDrop() {
        return false;
    }

    @SubscribeEvent
    public static void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        if (!Loader.isModLoaded("integratedtunnels")) return;
        if (event.getWorld().getBlockState(event.getPos()).getBlock() instanceof BlockAccessProxy) {
            if (event.getPlayer() instanceof ExtendedFakePlayer) {
                event.setCanceled(true);
            }
        }
    }
}
