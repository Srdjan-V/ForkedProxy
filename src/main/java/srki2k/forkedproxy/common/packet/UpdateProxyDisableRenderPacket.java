package srki2k.forkedproxy.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import srki2k.forkedproxy.client.data.AccessProxyClientData;

public class UpdateProxyDisableRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;
    @CodecField
    private boolean disable;

    public UpdateProxyDisableRenderPacket() {
    }

    public UpdateProxyDisableRenderPacket(int proxyDim, BlockPos proxyPos, boolean disable) {
        this.proxyDim = proxyDim;
        this.proxyPos = proxyPos;
        this.disable = disable;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.putDisable(proxyDim, proxyPos, disable);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
    }
}
