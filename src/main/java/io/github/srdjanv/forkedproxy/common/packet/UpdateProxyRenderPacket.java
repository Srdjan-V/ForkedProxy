package io.github.srdjanv.forkedproxy.common.packet;

import io.github.srdjanv.forkedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;
    @CodecField
    private BlockPos targetPos;

    public UpdateProxyRenderPacket() {
    }

    public UpdateProxyRenderPacket(int proxyDim, BlockPos proxyPos, BlockPos target) {
        this.proxyDim = proxyDim;
        this.proxyPos = proxyPos;
        targetPos = target;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.putTarget(proxyDim, proxyPos, targetPos);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
    }
}
