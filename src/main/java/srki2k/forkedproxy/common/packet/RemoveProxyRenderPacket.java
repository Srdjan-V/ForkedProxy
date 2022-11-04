package srki2k.forkedproxy.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import srki2k.forkedproxy.client.data.AccessProxyClientData;

public class RemoveProxyRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;

    public RemoveProxyRenderPacket() {
    }

    public RemoveProxyRenderPacket(int proxyDim, BlockPos proxyPos) {
        this.proxyDim = proxyDim;
        this.proxyPos = proxyPos;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.remove(proxyDim, proxyPos);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
    }
}
