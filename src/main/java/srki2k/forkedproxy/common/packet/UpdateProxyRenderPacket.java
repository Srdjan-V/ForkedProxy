package srki2k.forkedproxy.common.packet;

import srki2k.forkedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;
    @CodecField
    private BlockPos targetPos;
    @CodecField
    private int targetDim;

    public UpdateProxyRenderPacket() { }

    public UpdateProxyRenderPacket(DimPos proxy, DimPos target) {
        proxyPos = proxy.getBlockPos();
        proxyDim = proxy.getDimensionId();
        targetPos = target.getBlockPos();
        targetDim = target.getDimensionId();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.putTarget(
                DimPos.of(proxyDim, proxyPos),
                DimPos.of(targetDim, targetPos)
        );
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
