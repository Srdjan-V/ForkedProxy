package io.github.srdjanv.forkedproxy.common.packet;

import io.github.srdjanv.forkedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyDisplayRotationPacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;
    @CodecField
    private NBTTagCompound rotation;

    public UpdateProxyDisplayRotationPacket() {
    }

    public UpdateProxyDisplayRotationPacket(int proxyDim, BlockPos proxyPos, int[] rotation) {
        this.proxyPos = proxyPos;
        this.proxyDim = proxyDim;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setIntArray("rot", rotation);
        this.rotation = nbt;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.putRotation(proxyDim, proxyPos, rotation.getIntArray("rot"));
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
    }
}
