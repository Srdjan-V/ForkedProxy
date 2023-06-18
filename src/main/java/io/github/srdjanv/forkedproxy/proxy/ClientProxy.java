package io.github.srdjanv.forkedproxy.proxy;

import io.github.srdjanv.forkedproxy.ForkedProxy;
import io.github.srdjanv.forkedproxy.client.data.AccessProxyClientData;
import net.minecraftforge.common.MinecraftForge;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import io.github.srdjanv.forkedproxy.client.render.world.AccessProxyTargetRenderer;

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
        MinecraftForge.EVENT_BUS.register(AccessProxyClientData.class);
    }
}
