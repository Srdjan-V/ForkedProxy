package srki2k.forkedproxy.common.datamanegmant;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.packet.UpdateProxyDisableRenderPacket;
import srki2k.forkedproxy.common.packet.UpdateProxyDisplayRotationPacket;
import srki2k.forkedproxy.common.packet.UpdateProxyDisplayValuePacket;
import srki2k.forkedproxy.common.packet.UpdateProxyRenderPacket;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class WorldProxyManager {

    private WorldProxyManager() {
    }

    private static final Set<TileAccessProxy> existingTiles = new HashSet<>();

    public static void registerProxy(TileAccessProxy proxy) {
        existingTiles.add(proxy);
    }

    public static void unRegisterProxy(TileAccessProxy proxy) {
        existingTiles.remove(proxy);
    }


    public static TileAccessProxy getProxiesFromTarget(BlockPos target) {
        for (TileAccessProxy p : existingTiles) {
            if (p.target.getBlockPos().equals(target)) {
                return p;
            }
        }

        return null;
    }


    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            for (TileAccessProxy proxy : existingTiles) {
                ForkedProxy.INSTANCE.getPacketHandler().sendToPlayer(new UpdateProxyRenderPacket(DimPos.of(proxy.getWorld(), proxy.getPos()), proxy.target), (EntityPlayerMP) event.player);
                ForkedProxy.INSTANCE.getPacketHandler().sendToPlayer(new UpdateProxyDisplayValuePacket(DimPos.of(proxy.getWorld(), proxy.getPos()), proxy.getDisplayValue()), (EntityPlayerMP) event.player);
                ForkedProxy.INSTANCE.getPacketHandler().sendToPlayer(new UpdateProxyDisplayRotationPacket(DimPos.of(proxy.getWorld(), proxy.getPos()), proxy.display_rotations), (EntityPlayerMP) event.player);
                ForkedProxy.INSTANCE.getPacketHandler().sendToPlayer(new UpdateProxyDisableRenderPacket(DimPos.of(proxy.getWorld(), proxy.getPos()), proxy.disable_render), (EntityPlayerMP) event.player);

            }
        }
    }

    public static void cleanExistingTiles() {
        existingTiles.clear();
    }
}
