package com.breakinblocks.nautec.utils.codec;

import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public final class StreamCodecs {
    private StreamCodecs() {
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                T3 v3 = codec3.decode(buffer);
                T4 v4 = codec4.decode(buffer);
                T5 v5 = codec5.decode(buffer);
                T6 v6 = codec6.decode(buffer);
                T7 v7 = codec7.decode(buffer);
                return factory.apply(v1, v2, v3, v4, v5, v6, v7);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                T3 v3 = codec3.decode(buffer);
                T4 v4 = codec4.decode(buffer);
                T5 v5 = codec5.decode(buffer);
                T6 v6 = codec6.decode(buffer);
                T7 v7 = codec7.decode(buffer);
                T8 v8 = codec8.decode(buffer);
                return factory.apply(v1, v2, v3, v4, v5, v6, v7, v8);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
            }
        };
    }
}
