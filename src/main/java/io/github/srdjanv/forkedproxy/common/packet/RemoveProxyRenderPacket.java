package io.github.srdjanv.forkedproxy.common.packet;

import io.github.srdjanv.forkedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

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
