package cz.ladicek.dmrdiff;

import java.util.List;

final class CompoundDifference implements Diff {
    private final String address;
    private final List<Diff> differences;

    CompoundDifference(String address, List<Diff> differences) {
        if (differences.stream().anyMatch(diff -> diff.kind() == DiffKind.NONE)) {
            throw new IllegalArgumentException("Difference of kind NONE not allowed in compound difference: "
                    + differences);
        }

        this.address = address;
        this.differences = differences;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public DiffKind kind() {
        return DiffKind.COMPOUND;
    }

    @Override
    public String describe() {
        StringBuilder result = new StringBuilder();
        result.append(address).append("\n");
        for (Diff diff : differences) {
            result.append(diff.describe()).append("\n");
        }
        result.delete(result.length() - 1, result.length());
        return result.toString();
    }
}
