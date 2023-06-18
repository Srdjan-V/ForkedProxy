package io.github.srdjanv.forkedproxy.proxy;

import io.github.srdjanv.forkedproxy.ForkedProxy;
import io.github.srdjanv.forkedproxy.common.packet.*;
import net.minecraftforge.common.MinecraftForge;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import io.github.srdjanv.forkedproxy.common.datamanagement.WorldProxyManager;
import io.github.srdjanv.forkedproxy.common.packet.*;

public class CommonProxy extends CommonProxyComponent {
    @Override
    public ModBase getMod() {
        return ForkedProxy.INSTANCE;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);

        packetHandler.register(LoginProxyRenderPacket.class);
        packetHandler.register(RemoveProxyRenderPacket.class);
        packetHandler.register(UpdateProxyRenderPacket.class);
        packetHandler.register(UpdateProxyDisplayValuePacket.class);
        packetHandler.register(UpdateProxyDisplayRotationPacket.class);
        packetHandler.register(UpdateProxyDisableRenderPacket.class);
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(WorldProxyManager.class);
    }
}
