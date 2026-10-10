package com.breakinblocks.nautec.utils.valueio;

public interface ValueIOSerializable {
    void serialize(ValueOutput output);

    void deserialize(ValueInput input);
}
