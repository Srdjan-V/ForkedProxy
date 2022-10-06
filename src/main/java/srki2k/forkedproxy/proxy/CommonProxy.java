package srki2k.forkedproxy.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.packet.*;

public class CommonProxy extends CommonProxyComponent {
    @Override
    public ModBase getMod() {
        return ForkedProxy.INSTANCE;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);

        packetHandler.register(RemoveProxyRenderPacket.class);
        packetHandler.register(UpdateProxyRenderPacket.class);
        packetHandler.register(UpdateProxyDisplayValuePacket.class);
        packetHandler.register(UpdateProxyDisplayRotationPacket.class);
        packetHandler.register(UpdateProxyDisableRenderPacket.class);
    }
}
