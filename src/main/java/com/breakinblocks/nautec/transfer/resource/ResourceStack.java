package com.breakinblocks.nautec.transfer.resource;

public record ResourceStack<T extends Resource>(T resource, int amount) {
    public boolean isEmpty() {
        return resource.isEmpty() || amount <= 0;
    }

    @Override
    public String toString() {
        return amount + "x " + resource;
    }
}
