package srki2k.forkedproxy.client.data;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

public class ProxyPosData {

    private DimPos target;

    private IValue variable;

    private int[] rotation;

    private boolean isDisable;

    public ProxyPosData() {
    }

    public ProxyPosData(DimPos target, IValue variable, int[] rotation, boolean isDisable) {
        this.target = target;
        this.variable = variable;
        this.rotation = rotation;
        this.isDisable = isDisable;
    }

    public DimPos getTarget() {
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

    public void setTarget(DimPos target) {
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

