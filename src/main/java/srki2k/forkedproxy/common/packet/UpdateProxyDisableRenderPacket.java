package srki2k.forkedproxy.common.packet;

import srki2k.forkedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyDisableRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;
    @CodecField
    private boolean disable;

    public UpdateProxyDisableRenderPacket() { }

    public UpdateProxyDisableRenderPacket(DimPos proxy, boolean disable) {
        proxyPos = proxy.getBlockPos();
        proxyDim = proxy.getDimensionId();
        this.disable = disable;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.putDisable(DimPos.of(proxyDim, proxyPos), disable);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
