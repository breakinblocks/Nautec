package com.breakinblocks.nautec.transfer;

import com.breakinblocks.nautec.transfer.resource.Resource;

@FunctionalInterface
public interface IndexModifier<T extends Resource> {
    void set(int index, T resource, int amount);
}
