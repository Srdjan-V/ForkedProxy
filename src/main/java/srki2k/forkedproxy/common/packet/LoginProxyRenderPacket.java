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
    private BlockPos proxy_pos;
    @CodecField
    private int proxy_dim;
    @CodecField
    private BlockPos target_pos;
    @CodecField
    private int target_dim;
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

    public LoginProxyRenderPacket(DimPos proxy_pos, DimPos target_pos, boolean disable, int[] rotation, IValue value) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getDimensionId();
        this.target_pos = target_pos.getBlockPos();
        this.target_dim = target_pos.getDimensionId();
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
        AccessProxyClientData accessProxyClientData = AccessProxyClientData.getInstance();

        IValue variable = null;

        if (!nbt.isEmpty()) {
            variable = ValueHelpers.deserialize(nbt);
        }

        accessProxyClientData.putAll(
                DimPos.of(this.proxy_dim, this.proxy_pos),
                DimPos.of(this.target_dim, this.target_pos),
                this.disable,
                this.rotation.getIntArray("rot"),
                variable);

    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
