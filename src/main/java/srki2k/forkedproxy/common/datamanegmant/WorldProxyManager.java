package srki2k.forkedproxy.common.datamanegmant;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.packet.*;
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
                ForkedProxy.INSTANCE.getPacketHandler().sendToPlayer(
                        new LoginProxyRenderPacket(DimPos.of(proxy.getWorld(), proxy.getPos()),
                                proxy.target, proxy.disable_render, proxy.display_rotations, proxy.getDisplayValue()),
                        (EntityPlayerMP) event.player);
            }
        }
    }

    public static void cleanExistingTiles() {
        existingTiles.clear();
    }
}
