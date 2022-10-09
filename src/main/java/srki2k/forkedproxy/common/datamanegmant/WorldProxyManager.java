package srki2k.forkedproxy.common.datamanegmant;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.packet.LoginProxyRenderPacket;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ForkedProxy.MODID)
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
}
