package srki2k.forkedproxy.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import srki2k.forkedproxy.client.data.AccessProxyClientData;

public class RemoveProxyRenderPacket extends PacketCodec {
    @CodecField
    private DimPos proxy_pos;

    public RemoveProxyRenderPacket() {
    }

    public RemoveProxyRenderPacket(DimPos proxy_pos) {
        this.proxy_pos = proxy_pos;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.getInstance().remove(proxy_pos);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
    }
}
