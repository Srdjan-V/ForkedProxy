package srki2k.forkedproxy.client.data;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.Collection;
import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class AccessProxyClientData {

    private AccessProxyClientData() {
    }

    private static final AccessProxyClientData _instance = new AccessProxyClientData();

    public static AccessProxyClientData getInstance() {
        return _instance;
    }

    private final HashMap<DimPos, ProxyPosData> proxyPosDataHashMap = new HashMap<>();


    public void putAll(DimPos proxy, DimPos target, boolean disable, int[] rotation, IValue value) {
        proxyPosDataHashMap.put(proxy, new ProxyPosData(target, value, rotation, disable));
    }

    public void putTarget(DimPos proxy, DimPos target) {
        ProxyPosData proxyPosData = proxyPosDataHashMap.get(proxy);
        if (proxyPosData == null) {
            proxyPosData = new ProxyPosData();
            proxyPosDataHashMap.put(proxy, proxyPosData);
        }
        proxyPosData.setTarget(target);
    }

    public void putVariable(DimPos proxy, IValue variable) {
        ProxyPosData proxyPosData = proxyPosDataHashMap.get(proxy);
        if (proxyPosData == null) {
            proxyPosData = new ProxyPosData();
            proxyPosDataHashMap.put(proxy, proxyPosData);
        }
        proxyPosData.setVariable(variable);
    }

    public void putRotation(DimPos proxy, int[] rotation) {
        ProxyPosData proxyPosData = proxyPosDataHashMap.get(proxy);
        if (proxyPosData == null) {
            proxyPosData = new ProxyPosData();
            proxyPosDataHashMap.put(proxy, proxyPosData);
        }
        proxyPosData.setRotation(rotation);
    }

    public void putDisable(DimPos proxy, boolean disable) {
        ProxyPosData proxyPosData = proxyPosDataHashMap.get(proxy);
        if (proxyPosData == null) {
            proxyPosData = new ProxyPosData();
            proxyPosDataHashMap.put(proxy, proxyPosData);
        }
        proxyPosData.setDisable(disable);
    }


    public void remove(DimPos proxy) {
        proxyPosDataHashMap.remove(proxy);
    }

    public Collection<ProxyPosData> getProxy() {
        return proxyPosDataHashMap.values();
    }

    public ProxyPosData getProxy(DimPos proxyPos) {
        return proxyPosDataHashMap.get(proxyPos);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.world.isRemote && event.player.equals(Minecraft.getMinecraft().player)) {
            proxyPosDataHashMap.clear();
        }
    }
}
