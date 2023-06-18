package io.github.srdjanv.forkedproxy.common.datamanagement;

import io.github.srdjanv.forkedproxy.ForkedProxy;
import io.github.srdjanv.forkedproxy.common.packet.LoginProxyRenderPacket;
import io.github.srdjanv.forkedproxy.common.tileentity.TileAccessProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldProxyManager {
    private WorldProxyManager() {
    }

    private static final HashMap<Integer, List<TileAccessProxy>> dimProxyMap = new HashMap<>();

    public static void registerProxy(int dimensionId, TileAccessProxy proxy) {
        List<TileAccessProxy> tileAccessProxyList = dimProxyMap.get(dimensionId);

        if (tileAccessProxyList == null) {
            tileAccessProxyList = new ArrayList<>();
            dimProxyMap.put(dimensionId, tileAccessProxyList);
            proxy.getWorld().addEventListener(ProxyWorldEventListener.getInstance());
        }

        tileAccessProxyList.add(proxy);
    }

    public static void unRegisterProxy(int dimensionId, TileAccessProxy proxy) {
        List<TileAccessProxy> tileAccessProxyList = dimProxyMap.get(dimensionId);
        tileAccessProxyList.remove(proxy);

        if (tileAccessProxyList.isEmpty()) {
            dimProxyMap.remove(dimensionId);
            proxy.getWorld().removeEventListener(ProxyWorldEventListener.getInstance());
        }
    }

    public static TileAccessProxy getRedstoneProxiesFromTarget(int dimensionId, BlockPos target) {
        List<TileAccessProxy> tileAccessProxyList = dimProxyMap.get(dimensionId);
        if (tileAccessProxyList == null) {
            return null;
        }
        for (TileAccessProxy tileAccessProxy : tileAccessProxyList) {
            if (tileAccessProxy.getTarget() != null && tileAccessProxy.getTarget().getBlockPos().equals(target)) {
                return tileAccessProxy;
            }
        }

        return null;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) {
            return;
        }

        NBTTagCompound topNbt = new NBTTagCompound();
        for (Integer proxyDim : dimProxyMap.keySet()) {
            List<TileAccessProxy> proxyList = dimProxyMap.get(proxyDim);

            NBTTagCompound allProxysInDimNbt = new NBTTagCompound();
            topNbt.setTag(String.valueOf(proxyDim), allProxysInDimNbt);

            for (int i = 0; i < proxyList.size(); i++) {
                TileAccessProxy proxy = proxyList.get(i);

                NBTTagCompound proxyNbt = new NBTTagCompound();
                allProxysInDimNbt.setTag("proxyID:" + i, proxyNbt);

                int[] proxyPos = {proxy.getPos().getX(), proxy.getPos().getY(), proxy.getPos().getZ()};
                proxyNbt.setIntArray("proxyPos", proxyPos);

                if (proxy.getTarget() != null) {
                    proxyNbt.setIntArray("proxyTarget",
                            new int[]{proxy.getTarget().getBlockPos().getX(),
                                    proxy.getTarget().getBlockPos().getY(),
                                    proxy.getTarget().getBlockPos().getZ()});
                }

                proxyNbt.setBoolean("disableRender", proxy.isDisableRender());
                proxyNbt.setIntArray("displayRotations", proxy.getDisplayRotations());

                IValue displayValue = proxy.getDisplayValue();
                if (displayValue == null) {
                    proxyNbt.setTag("displayValue", new NBTTagCompound());
                } else {
                    proxyNbt.setTag("displayValue", ValueHelpers.serialize(displayValue));
                }
            }
        }

        ForkedProxy.INSTANCE.getPacketHandler().sendToPlayer(new LoginProxyRenderPacket(topNbt), (EntityPlayerMP) event.player);
    }

    public static void cleanExistingTiles() {
        dimProxyMap.clear();
    }

    private static class ProxyWorldEventListener implements IWorldEventListener {
        private static ProxyWorldEventListener proxyWorldEventListener;

        public static ProxyWorldEventListener getInstance() {
            if (proxyWorldEventListener == null) {
                proxyWorldEventListener = new ProxyWorldEventListener();
            }

            return proxyWorldEventListener;
        }

        @Override
        public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
            List<TileAccessProxy> proxies = dimProxyMap.get(worldIn.provider.getDimension());
            if (proxies == null) {
                return;
            }
            for (TileAccessProxy proxy : proxies) {
                if (!pos.equals(proxy.getPos()) && proxy.getTarget() != null && pos.equals(proxy.getTarget().getBlockPos())) {
                    proxy.updateProxyAfterTargetChange();
                }
            }
        }

        @Override
        public void notifyLightSet(BlockPos pos) {
        }

        @Override
        public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        }

        @Override
        public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        }

        @Override
        public void playRecord(SoundEvent soundIn, BlockPos pos) {
        }

        @Override
        public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        }

        @Override
        public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        }

        @Override
        public void onEntityAdded(Entity entityIn) {
        }

        @Override
        public void onEntityRemoved(Entity entityIn) {
        }

        @Override
        public void broadcastSound(int soundID, BlockPos pos, int data) {
        }

        @Override
        public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
        }

        @Override
        public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        }


    }

}
