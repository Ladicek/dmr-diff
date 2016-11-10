package cz.ladicek.dmrdiff;

public interface Diff {
    String address();
    DiffKind kind();
    String describe();
}
