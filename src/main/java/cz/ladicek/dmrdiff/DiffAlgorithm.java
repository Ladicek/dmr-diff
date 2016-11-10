package cz.ladicek.dmrdiff;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DiffAlgorithm {
    public static Diff compute(ModelNode oldInput, ModelNode newInput) {
        return compute(oldInput, newInput, "");
    }

    private static Diff compute(ModelNode oldInput, ModelNode newInput, String address) {
        if (oldInput.getType() != newInput.getType()) {
            return new BasicDifference(address, oldInput, newInput);
        }

        ModelType type = oldInput.getType();
        if (isBasicType(type)) {
            if (!oldInput.equals(newInput)) {
                return new BasicDifference(address, oldInput, newInput);
            }
        } else if (type == ModelType.LIST) {
            List<ModelNode> oldList = oldInput.asList();
            List<ModelNode> newList = newInput.asList();

            int size = Math.max(oldList.size(), newList.size());

            List<Diff> diffs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ModelNode oldElement = getFromList(oldList, i);
                ModelNode newElement = getFromList(newList, i);
                Diff diff = compute(oldElement, newElement, address + "[" + i + "]");
                if (diff.kind() != DiffKind.NONE) {
                    diffs.add(diff);
                }
            }
            if (!diffs.isEmpty()) {
                return new CompoundDifference(address, diffs);
            }
        } else if (type == ModelType.OBJECT) {
            List<Property> oldObject = oldInput.asPropertyList();
            List<Property> newObject = newInput.asPropertyList();

            List<String> keys = Stream.concat(oldObject.stream(), newObject.stream())
                    .map(Property::getName)
                    .distinct()
                    .collect(Collectors.toList());

            List<Diff> diffs = new ArrayList<>(keys.size());
            for (String key : keys) {
                ModelNode oldElement = getFromObject(oldInput, key);
                ModelNode newElement = getFromObject(newInput, key);
                Diff diff = compute(oldElement, newElement, address + "/" + key);
                if (diff.kind() != DiffKind.NONE) {
                    diffs.add(diff);
                }
            }
            if (!diffs.isEmpty()) {
                return new CompoundDifference(address, diffs);
            }
        } else if (type == ModelType.PROPERTY) {
            String oldName = oldInput.asProperty().getName();
            String newName = newInput.asProperty().getName();
            if (!oldName.equals(newName)) {
                return new BasicDifference(address, oldInput, newInput);
            } else {
                ModelNode oldValue = oldInput.asProperty().getValue();
                ModelNode newValue = newInput.asProperty().getValue();
                return compute(oldValue, newValue, address + "/" + oldName);
            }
        } else {
            throw new IllegalStateException("Unexpected input type: " + type);
        }

        return new NoDifference(address);
    }

    private static ModelNode getFromList(List<ModelNode> list, int index) {
        if (index < list.size()) {
            return list.get(index);
        }

        return new ModelNode(); // undefined
    }

    private static ModelNode getFromObject(ModelNode object, String key) {
        if (object.has(key)) {
            return object.get(key);
        }

        return new ModelNode(); // undefined
    }

    static boolean isBasicType(ModelType modelType) {
        // it's a switch with a list of all known types, because in case a future DMR version adds a new type,
        // I really want to fail rather than blindly promote it to a basic type (or a compound type)
        switch (modelType) {
            case BIG_DECIMAL:
            case BIG_INTEGER:
            case BOOLEAN:
            case BYTES:
            case DOUBLE:
            case EXPRESSION:
            case INT:
            case LONG:
            case STRING:
            case TYPE:
            case UNDEFINED:
                return true;
            case LIST:
            case OBJECT:
            case PROPERTY:
                return false;
            default:
                throw new IllegalArgumentException("Unknown model type " + modelType);
        }
    }
}
