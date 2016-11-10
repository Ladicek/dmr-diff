package cz.ladicek.dmrdiff;

final class NoDifference implements Diff {
    private final String address;

    NoDifference(String address) {
        this.address = address;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public DiffKind kind() {
        return DiffKind.NONE;
    }

    @Override
    public String describe() {
        return address + ": <no difference>";
    }
}
