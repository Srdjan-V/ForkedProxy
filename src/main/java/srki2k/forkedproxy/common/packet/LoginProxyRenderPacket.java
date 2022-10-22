package srki2k.forkedproxy.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import srki2k.forkedproxy.client.data.AccessProxyClientData;

public class LoginProxyRenderPacket extends PacketCodec {

    @CodecField
    private BlockPos proxyPos;
    @CodecField
    private int proxyDim;
    @CodecField
    private BlockPos targetPos;
    @CodecField
    private int targetDim;
    @CodecField
    private NBTTagCompound nbt;
    @CodecField
    private NBTTagCompound rotation;
    @CodecField
    private boolean disable;

    public LoginProxyRenderPacket() {
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    public LoginProxyRenderPacket(DimPos proxyDimPos, DimPos targetDimPos, boolean disable, int[] rotation, IValue value) {
        this.proxyPos = proxyDimPos.getBlockPos();
        this.proxyDim = proxyDimPos.getDimensionId();
        this.targetPos = targetDimPos.getBlockPos();
        this.targetDim = targetDimPos.getDimensionId();
        this.disable = disable;

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setIntArray("rot", rotation);
        this.rotation = nbt;

        if (value == null) {
            this.nbt = new NBTTagCompound();
            return;
        }
        this.nbt = ValueHelpers.serialize(value);
    }


    @Override
    public void actionClient(World world, EntityPlayer player) {
        IValue variable = null;

        if (!nbt.isEmpty()) {
            variable = ValueHelpers.deserialize(nbt);
        }

        AccessProxyClientData.getInstance().putAll(
                DimPos.of(this.proxyDim, this.proxyPos),
                DimPos.of(this.targetDim, this.targetPos),
                this.disable,
                this.rotation.getIntArray("rot"),
                variable);

    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
