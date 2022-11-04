package srki2k.forkedproxy.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import srki2k.forkedproxy.client.data.AccessProxyClientData;

public class UpdateProxyDisplayValuePacket extends PacketCodec {
    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;
    @CodecField
    private NBTTagCompound nbt;

    public UpdateProxyDisplayValuePacket() {
    }

    public UpdateProxyDisplayValuePacket(int proxyDim, BlockPos proxyPos, IValue value) {
        this.proxyDim = proxyDim;
        this.proxyPos = proxyPos;
        if (value == null) {
            this.nbt = new NBTTagCompound();
            return;
        }
        this.nbt = ValueHelpers.serialize(value);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        if (nbt.isEmpty()) {
            AccessProxyClientData.putVariable(proxyDim, proxyPos, null);
            return;
        }
        AccessProxyClientData.putVariable(proxyDim, proxyPos, ValueHelpers.deserialize(nbt));
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
    }
}
