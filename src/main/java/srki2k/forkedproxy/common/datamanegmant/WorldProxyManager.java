package srki2k.forkedproxy.common.datamanegmant;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.packet.LoginProxyRenderPacket;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ForkedProxy.MODID)
public class WorldProxyManager {

    private WorldProxyManager() {
    }

    private static final Set<TileAccessProxy> existingTiles = new HashSet<>();


    // TODO: 16/10/2022 Find a better way of doing this
    private static final IWorldEventListener proxyWorldEventListener = new ProxyWorldEventListener();

    static {
        for (World world : DimensionManager.getWorlds()) {
            world.addEventListener(proxyWorldEventListener);
        }
    }

    public static void registerProxy(TileAccessProxy proxy) {
        existingTiles.add(proxy);
    }

    public static void unRegisterProxy(TileAccessProxy proxy) {
        existingTiles.remove(proxy);
    }

    public static TileAccessProxy getRedstoneProxiesFromTarget(DimPos target) {
        for (TileAccessProxy p : existingTiles) {
            if (target.equals(p.target)) {
                return p;
            }
        }

        return null;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            for (TileAccessProxy proxy : existingTiles) {
                //Checking for null why don't know, but this fixes NPEs
                if (proxy.target == null) {
                    DimPos proxyPosTarget = DimPos.of(proxy.getWorld(), proxy.getPos());
                    ForkedProxy.INSTANCE.getPacketHandler().sendToPlayer(
                            new LoginProxyRenderPacket(proxyPosTarget, proxyPosTarget, proxy.disable_render, proxy.display_rotations, proxy.getDisplayValue()),
                            (EntityPlayerMP) event.player);
                    continue;
                }

                ForkedProxy.INSTANCE.getPacketHandler().sendToPlayer(
                        new LoginProxyRenderPacket(DimPos.of(proxy.getWorld(), proxy.getPos()), proxy.target, proxy.disable_render, proxy.display_rotations, proxy.getDisplayValue()),
                        (EntityPlayerMP) event.player);

            }
        }
    }

    public static void cleanExistingTiles() {
        existingTiles.clear();
    }

    private static class ProxyWorldEventListener implements IWorldEventListener {
        @Override
        public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
            for (TileAccessProxy proxy : existingTiles) {
                if (proxy.target != null && pos.equals(proxy.target.getBlockPos()) && worldIn.equals(proxy.target.getWorld())) {
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
