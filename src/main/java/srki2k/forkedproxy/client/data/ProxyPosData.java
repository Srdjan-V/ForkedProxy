package srki2k.forkedproxy.client.data;

import net.minecraft.util.math.BlockPos;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

public class ProxyPosData {

    private BlockPos target;

    private IValue variable;

    private int[] rotation;

    private boolean isDisable;

    public ProxyPosData() {
        this.rotation = new int[6];
    }

    public ProxyPosData(BlockPos target, IValue variable, int[] rotation, boolean isDisable) {
        this.target = target;
        this.variable = variable;
        this.rotation = rotation;
        this.isDisable = isDisable;
    }

    public BlockPos getTarget() {
        return target;
    }

    public IValue getVariable() {
        return variable;
    }

    public int[] getRotation() {
        return rotation;
    }

    public boolean isDisable() {
        return isDisable;
    }

    public void setTarget(BlockPos target) {
        this.target = target;
    }

    public void setVariable(IValue variable) {
        this.variable = variable;
    }

    public void setRotation(int[] rotation) {
        this.rotation = rotation;
    }

    public void setDisable(boolean disable) {
        isDisable = disable;
    }

}

