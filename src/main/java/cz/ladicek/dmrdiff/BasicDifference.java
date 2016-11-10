package cz.ladicek.dmrdiff;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import static cz.ladicek.dmrdiff.DiffAlgorithm.isBasicType;

final class BasicDifference implements Diff {
    private final String address;
    private final String oldValueAsString;
    private final String newValueAsString;

    private static String describeValue(ModelNode value) {
        ModelType type = value.getType();
        if (type == ModelType.UNDEFINED) {
            return type.name();
        } else if (isBasicType(type)) {
            return type.name() + " '" + value.asString() + "'";
        } else if (type == ModelType.PROPERTY) {
            return type.name() + " '" + value.asProperty().getName() + "'";
        } else if (type == ModelType.LIST || type == ModelType.OBJECT) {
            return type.name();
        } else {
            throw new IllegalArgumentException("Unknown value type " + type);
        }
    }

    BasicDifference(String address, ModelNode oldValue, ModelNode newValue) {
        this.address = address;
        this.oldValueAsString = describeValue(oldValue);
        this.newValueAsString = describeValue(newValue);
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public DiffKind kind() {
        return DiffKind.BASIC;
    }

    @Override
    public String describe() {
        return address + ": old is " + oldValueAsString + ", new is " + newValueAsString;
    }
}
