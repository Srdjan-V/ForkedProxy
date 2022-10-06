package srki2k.forkedproxy.proxy;

import net.minecraftforge.common.MinecraftForge;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.client.data.AccessProxyClientData;
import srki2k.forkedproxy.client.render.world.AccessProxyTargetRenderer;

public class ClientProxy extends ClientProxyComponent {
    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return ForkedProxy.INSTANCE;
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(AccessProxyTargetRenderer.class);
        MinecraftForge.EVENT_BUS.register(AccessProxyClientData.getInstance());
    }
}
