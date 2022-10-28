package srki2k.forkedproxy.common.packet;

import srki2k.forkedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyDisplayRotationPacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;
    @CodecField
    private NBTTagCompound rotation;

    public UpdateProxyDisplayRotationPacket() { }

    public UpdateProxyDisplayRotationPacket(DimPos proxy, int[] rotation) {
        proxyPos = proxy.getBlockPos();
        proxyDim = proxy.getDimensionId();
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
        AccessProxyClientData.putRotation(DimPos.of(proxyDim, proxyPos), rotation.getIntArray("rot"));
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
