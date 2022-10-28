package srki2k.forkedproxy.common.packet;

import srki2k.forkedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class RemoveProxyRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;

    public RemoveProxyRenderPacket() { }

    public RemoveProxyRenderPacket(DimPos proxy) {
        proxyPos = proxy.getBlockPos();
        proxyDim = proxy.getDimensionId();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.remove(
                DimPos.of(proxyDim, proxyPos)
        );
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
