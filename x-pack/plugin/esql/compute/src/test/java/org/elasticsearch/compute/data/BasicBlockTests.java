/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.BytesRefArray;
import org.elasticsearch.test.ESTestCase;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class BasicBlockTests extends ESTestCase {

    public void testEmpty() {
        assertThat(new IntArrayBlock(new int[] {}, 0, new int[] {}, new BitSet()).getPositionCount(), is(0));
        assertThat(IntBlock.newBlockBuilder(0).build().getPositionCount(), is(0));
        assertThat(new IntArrayVector(new int[] {}, 0, null).getPositionCount(), is(0));
        assertThat(IntVector.newVectorBuilder(0).build().getPositionCount(), is(0));

        assertThat(new LongArrayBlock(new long[] {}, 0, new int[] {}, new BitSet()).getPositionCount(), is(0));
        assertThat(LongBlock.newBlockBuilder(0).build().getPositionCount(), is(0));
        assertThat(new LongArrayVector(new long[] {}, 0).getPositionCount(), is(0));
        assertThat(LongVector.newVectorBuilder(0).build().getPositionCount(), is(0));

        assertThat(new DoubleArrayBlock(new double[] {}, 0, new int[] {}, new BitSet()).getPositionCount(), is(0));
        assertThat(DoubleBlock.newBlockBuilder(0).build().getPositionCount(), is(0));
        assertThat(new DoubleArrayVector(new double[] {}, 0).getPositionCount(), is(0));
        assertThat(DoubleVector.newVectorBuilder(0).build().getPositionCount(), is(0));

        var emptyArray = new BytesRefArray(0, BigArrays.NON_RECYCLING_INSTANCE);
        assertThat(new BytesRefArrayBlock(emptyArray, 0, new int[] {}, new BitSet()).getPositionCount(), is(0));
        assertThat(BytesRefBlock.newBlockBuilder(0).build().getPositionCount(), is(0));
        assertThat(new BytesRefArrayVector(emptyArray, 0).getPositionCount(), is(0));
        assertThat(BytesRefVector.newVectorBuilder(0).build().getPositionCount(), is(0));

        assertThat(new BooleanArrayBlock(new boolean[] {}, 0, new int[] {}, new BitSet()).getPositionCount(), is(0));
        assertThat(BooleanBlock.newBlockBuilder(0).build().getPositionCount(), is(0));
        assertThat(new BooleanArrayVector(new boolean[] {}, 0).getPositionCount(), is(0));
        assertThat(BooleanVector.newVectorBuilder(0).build().getPositionCount(), is(0));
    }

    public void testSmallSingleValueDenseGrowthInt() {
        for (int initialSize : List.of(0, 1, 2, 3, 4, 5)) {
            var blockBuilder = IntBlock.newBlockBuilder(initialSize);
            IntStream.range(0, 10).forEach(blockBuilder::appendInt);
            assertSingleValueDenseBlock(blockBuilder.build());
        }
    }

    public void testSmallSingleValueDenseGrowthLong() {
        for (int initialSize : List.of(0, 1, 2, 3, 4, 5)) {
            var blockBuilder = LongBlock.newBlockBuilder(initialSize);
            IntStream.range(0, 10).forEach(blockBuilder::appendLong);
            assertSingleValueDenseBlock(blockBuilder.build());
        }
    }

    public void testSmallSingleValueDenseGrowthDouble() {
        for (int initialSize : List.of(0, 1, 2, 3, 4, 5)) {
            var blockBuilder = DoubleBlock.newBlockBuilder(initialSize);
            IntStream.range(0, 10).forEach(blockBuilder::appendDouble);
            assertSingleValueDenseBlock(blockBuilder.build());
        }
    }

    public void testSmallSingleValueDenseGrowthBytesRef() {
        final BytesRef NULL_VALUE = new BytesRef();
        for (int initialSize : List.of(0, 1, 2, 3, 4, 5)) {
            var blockBuilder = BytesRefBlock.newBlockBuilder(initialSize);
            IntStream.range(0, 10).mapToObj(i -> NULL_VALUE).forEach(blockBuilder::appendBytesRef);
            assertSingleValueDenseBlock(blockBuilder.build());
        }
    }

    public void testSmallSingleValueDenseGrowthBoolean() {
        for (int initialSize : List.of(0, 1, 2, 3, 4, 5)) {
            var blockBuilder = BooleanBlock.newBlockBuilder(initialSize);
            IntStream.range(0, 10).forEach(i -> blockBuilder.appendBoolean(i % 3 == 0));
            assertSingleValueDenseBlock(blockBuilder.build());
        }
    }

    private static void assertSingleValueDenseBlock(Block initialBlock) {
        final int positionCount = initialBlock.getPositionCount();
        int depth = randomIntBetween(1, 5);
        for (int d = 0; d < depth; d++) {
            Block block = initialBlock;
            assertThat(block.getTotalValueCount(), is(positionCount));
            assertThat(block.getPositionCount(), is(positionCount));
            for (int j = 0; j < 10; j++) {
                int pos = randomPosition(positionCount);
                assertThat(block.getFirstValueIndex(pos), is(pos));
                assertThat(block.getValueCount(pos), is(1));
                assertThat(block.isNull(pos), is(false));
            }
            assertThat(block.asVector().getPositionCount(), is(positionCount));
            assertThat(block.asVector().asBlock().getPositionCount(), is(positionCount));
            assertThat(block.nullValuesCount(), is(0));
            assertThat(block.mayHaveNulls(), is(false));
            assertThat(block.areAllValuesNull(), is(false));
            assertThat(block.validPositionCount(), is(block.getPositionCount()));

            initialBlock = block.asVector().asBlock();
        }
    }

    public void testIntBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            IntBlock block;
            if (randomBoolean()) {
                final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
                IntBlock.Builder blockBuilder = IntBlock.newBlockBuilder(builderEstimateSize);
                IntStream.range(0, positionCount).forEach(blockBuilder::appendInt);
                block = blockBuilder.build();
            } else {
                block = new IntArrayVector(IntStream.range(0, positionCount).toArray(), positionCount, null).asBlock();
            }

            assertThat(positionCount, is(block.getPositionCount()));
            assertThat(0, is(block.getInt(0)));
            assertThat(positionCount - 1, is(block.getInt(positionCount - 1)));
            int pos = block.getInt(randomPosition(positionCount));
            assertThat(pos, is(block.getInt(pos)));
            assertSingleValueDenseBlock(block);

            if (positionCount > 1) {
                assertNullValues(
                    positionCount,
                    size -> IntBlock.newBlockBuilder(size),
                    (bb, value) -> bb.appendInt(value),
                    position -> position,
                    IntBlock.Builder::build,
                    (randomNonNullPosition, b) -> {
                        assertThat((int) randomNonNullPosition, is(b.getInt(randomNonNullPosition.intValue())));
                    }
                );
            }

            IntVector.Builder blockBuilder = IntVector.newVectorBuilder(
                randomBoolean() ? randomIntBetween(1, positionCount) : positionCount
            );
            IntStream.range(0, positionCount).forEach(blockBuilder::appendInt);
            IntVector vector = blockBuilder.build();
            assertSingleValueDenseBlock(vector.asBlock());
        }
    }

    public void testConstantIntBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            int value = randomInt();
            IntBlock block;
            if (randomBoolean()) {
                block = IntBlock.newConstantBlockWith(value, positionCount);
            } else {
                block = new ConstantIntVector(value, positionCount).asBlock();
            }
            assertThat(positionCount, is(block.getPositionCount()));
            assertThat(value, is(block.getInt(0)));
            assertThat(value, is(block.getInt(positionCount - 1)));
            assertThat(value, is(block.getInt(randomPosition(positionCount))));
            assertThat(block.isNull(randomPosition(positionCount)), is(false));
            assertSingleValueDenseBlock(block);
        }
    }

    public void testLongBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            LongBlock block;
            if (randomBoolean()) {
                final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
                LongBlock.Builder blockBuilder = LongBlock.newBlockBuilder(builderEstimateSize);
                LongStream.range(0, positionCount).forEach(blockBuilder::appendLong);
                block = blockBuilder.build();
            } else {
                block = new LongArrayVector(LongStream.range(0, positionCount).toArray(), positionCount).asBlock();
            }

            assertThat(positionCount, is(block.getPositionCount()));
            assertThat(0L, is(block.getLong(0)));
            assertThat((long) positionCount - 1, is(block.getLong(positionCount - 1)));
            int pos = (int) block.getLong(randomPosition(positionCount));
            assertThat((long) pos, is(block.getLong(pos)));
            assertSingleValueDenseBlock(block);

            if (positionCount > 1) {
                assertNullValues(
                    positionCount,
                    size -> LongBlock.newBlockBuilder(size),
                    (bb, value) -> bb.appendLong(value),
                    position -> (long) position,
                    LongBlock.Builder::build,
                    (randomNonNullPosition, b) -> {
                        assertThat((long) randomNonNullPosition, is(b.getLong(randomNonNullPosition.intValue())));
                    }
                );
            }

            LongVector.Builder blockBuilder = LongVector.newVectorBuilder(
                randomBoolean() ? randomIntBetween(1, positionCount) : positionCount
            );
            LongStream.range(0, positionCount).forEach(blockBuilder::appendLong);
            LongVector vector = blockBuilder.build();
            assertSingleValueDenseBlock(vector.asBlock());
        }
    }

    public void testConstantLongBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            long value = randomLong();
            LongBlock block;
            if (randomBoolean()) {
                block = LongBlock.newConstantBlockWith(value, positionCount);
            } else {
                block = new ConstantLongVector(value, positionCount).asBlock();
            }
            assertThat(positionCount, is(block.getPositionCount()));
            assertThat(value, is(block.getLong(0)));
            assertThat(value, is(block.getLong(positionCount - 1)));
            assertThat(value, is(block.getLong(randomPosition(positionCount))));
            assertThat(block.isNull(randomPosition(positionCount)), is(false));
            assertSingleValueDenseBlock(block);
        }
    }

    public void testDoubleBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            DoubleBlock block;
            if (randomBoolean()) {
                final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
                var blockBuilder = DoubleBlock.newBlockBuilder(builderEstimateSize);
                LongStream.range(0, positionCount).asDoubleStream().forEach(blockBuilder::appendDouble);
                block = blockBuilder.build();
            } else {
                block = new DoubleArrayVector(LongStream.range(0, positionCount).asDoubleStream().toArray(), positionCount).asBlock();
            }

            assertThat(positionCount, is(block.getPositionCount()));
            assertThat(0d, is(block.getDouble(0)));
            assertThat((double) positionCount - 1, is(block.getDouble(positionCount - 1)));
            int pos = (int) block.getDouble(randomPosition(positionCount));
            assertThat((double) pos, is(block.getDouble(pos)));
            assertSingleValueDenseBlock(block);

            if (positionCount > 1) {
                assertNullValues(
                    positionCount,
                    size -> DoubleBlock.newBlockBuilder(size),
                    (bb, value) -> bb.appendDouble(value),
                    position -> (double) position,
                    DoubleBlock.Builder::build,
                    (randomNonNullPosition, b) -> {
                        assertThat((double) randomNonNullPosition, is(b.getDouble(randomNonNullPosition.intValue())));
                    }
                );
            }

            DoubleVector.Builder blockBuilder = DoubleVector.newVectorBuilder(
                randomBoolean() ? randomIntBetween(1, positionCount) : positionCount
            );
            IntStream.range(0, positionCount).mapToDouble(ii -> 1.0 / ii).forEach(blockBuilder::appendDouble);
            DoubleVector vector = blockBuilder.build();
            assertSingleValueDenseBlock(vector.asBlock());
        }
    }

    public void testConstantDoubleBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            double value = randomDouble();
            DoubleBlock block;
            if (randomBoolean()) {
                block = DoubleBlock.newConstantBlockWith(value, positionCount);
            } else {
                block = new ConstantDoubleVector(value, positionCount).asBlock();
            }
            assertThat(positionCount, is(block.getPositionCount()));
            assertThat(value, is(block.getDouble(0)));
            assertThat(value, is(block.getDouble(positionCount - 1)));
            assertThat(value, is(block.getDouble(randomPosition(positionCount))));
            assertSingleValueDenseBlock(block);
        }
    }

    public void testBytesRefBlock() {
        int positionCount = randomIntBetween(1, 16 * 1024);
        BytesRef[] values = new BytesRef[positionCount];
        for (int i = 0; i < positionCount; i++) {
            BytesRef bytesRef = new BytesRef(randomByteArrayOfLength(between(1, 20)));
            if (bytesRef.length > 0 && randomBoolean()) {
                bytesRef.offset = randomIntBetween(0, bytesRef.length - 1);
                bytesRef.length = randomIntBetween(0, bytesRef.length - bytesRef.offset);
            }
            values[i] = bytesRef;
        }

        BytesRefBlock block;
        if (randomBoolean()) {
            final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
            var blockBuilder = BytesRefBlock.newBlockBuilder(builderEstimateSize);
            Arrays.stream(values).map(obj -> randomBoolean() ? obj : BytesRef.deepCopyOf(obj)).forEach(blockBuilder::appendBytesRef);
            block = blockBuilder.build();
        } else {
            BytesRefArray array = new BytesRefArray(0, BigArrays.NON_RECYCLING_INSTANCE);
            Arrays.stream(values).forEach(array::append);
            block = new BytesRefArrayVector(array, positionCount).asBlock();
        }

        assertThat(positionCount, is(block.getPositionCount()));
        BytesRef bytes = new BytesRef();
        for (int i = 0; i < positionCount; i++) {
            int pos = randomIntBetween(0, positionCount - 1);
            bytes = block.getBytesRef(pos, bytes);
            assertThat(bytes, equalTo(values[pos]));
        }
        assertSingleValueDenseBlock(block);

        if (positionCount > 1) {
            assertNullValues(
                positionCount,
                size -> BytesRefBlock.newBlockBuilder(size),
                (bb, value) -> bb.appendBytesRef(value),
                position -> values[position],
                BytesRefBlock.Builder::build,
                (randomNonNullPosition, b) -> assertThat(
                    values[randomNonNullPosition],
                    is(b.getBytesRef(randomNonNullPosition, new BytesRef()))
                )
            );
        }

        BytesRefVector.Builder blockBuilder = BytesRefVector.newVectorBuilder(
            randomBoolean() ? randomIntBetween(1, positionCount) : positionCount
        );
        IntStream.range(0, positionCount).mapToObj(ii -> new BytesRef(randomAlphaOfLength(5))).forEach(blockBuilder::appendBytesRef);
        BytesRefVector vector = blockBuilder.build();
        assertSingleValueDenseBlock(vector.asBlock());
    }

    public void testBytesRefBlockBuilderWithNulls() {
        int positionCount = randomIntBetween(0, 16 * 1024);
        final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
        var blockBuilder = BytesRefBlock.newBlockBuilder(builderEstimateSize);
        BytesRef[] values = new BytesRef[positionCount];
        for (int i = 0; i < positionCount; i++) {
            if (randomBoolean()) {
                // Add random sparseness
                blockBuilder.appendNull();
                values[i] = null;
            } else {
                BytesRef bytesRef = new BytesRef(randomByteArrayOfLength(between(1, 20)));
                if (bytesRef.length > 0 && randomBoolean()) {
                    bytesRef.offset = randomIntBetween(0, bytesRef.length - 1);
                    bytesRef.length = randomIntBetween(0, bytesRef.length - bytesRef.offset);
                }
                values[i] = bytesRef;
                if (randomBoolean()) {
                    bytesRef = BytesRef.deepCopyOf(bytesRef);
                }
                blockBuilder.appendBytesRef(bytesRef);
            }
        }
        BytesRefBlock block = blockBuilder.build();
        assertThat(positionCount, is(block.getPositionCount()));
        BytesRef bytes = new BytesRef();
        for (int i = 0; i < positionCount; i++) {
            int pos = randomIntBetween(0, positionCount - 1);
            bytes = block.getBytesRef(pos, bytes);
            if (values[pos] == null) {
                assertThat(block.isNull(pos), equalTo(true));
                assertThat(bytes, equalTo(new BytesRef()));
            } else {
                assertThat(bytes, equalTo(values[pos]));
                assertThat(block.getBytesRef(pos, bytes), equalTo(values[pos]));
            }
        }
    }

    public void testConstantBytesRefBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            BytesRef value = new BytesRef(randomByteArrayOfLength(between(1, 20)));
            BytesRefBlock block;
            if (randomBoolean()) {
                block = BytesRefBlock.newConstantBlockWith(value, positionCount);
            } else {
                block = new ConstantBytesRefVector(value, positionCount).asBlock();
            }
            assertThat(block.getPositionCount(), is(positionCount));

            BytesRef bytes = new BytesRef();
            bytes = block.getBytesRef(0, bytes);
            assertThat(bytes, is(value));
            bytes = block.getBytesRef(positionCount - 1, bytes);
            assertThat(bytes, is(value));
            bytes = block.getBytesRef(randomPosition(positionCount), bytes);
            assertThat(bytes, is(value));
            assertSingleValueDenseBlock(block);
        }
    }

    public void testBooleanBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            BooleanBlock block;
            if (randomBoolean()) {
                final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
                var blockBuilder = BooleanBlock.newBlockBuilder(builderEstimateSize);
                IntStream.range(0, positionCount).forEach(p -> blockBuilder.appendBoolean(p % 10 == 0));
                block = blockBuilder.build();
            } else {
                boolean[] values = new boolean[positionCount];
                for (int p = 0; p < positionCount; p++) {
                    values[p] = p % 10 == 0;
                }
                block = new BooleanArrayVector(values, positionCount).asBlock();
            }

            assertThat(block.getPositionCount(), is(positionCount));
            assertThat(block.getBoolean(0), is(true));
            assertThat(block.getBoolean(positionCount - 1), is((positionCount - 1) % 10 == 0));
            assertSingleValueDenseBlock(block);

            if (positionCount > 1) {
                assertNullValues(
                    positionCount,
                    BooleanBlock::newBlockBuilder,
                    (bb, value) -> bb.appendBoolean(value),
                    position -> position % 10 == 0,
                    BooleanBlock.Builder::build,
                    (randomNonNullPosition, b) -> {
                        assertThat(b.getBoolean(randomNonNullPosition.intValue()), is(randomNonNullPosition % 10 == 0));
                    }
                );
            }

            DoubleVector.Builder blockBuilder = DoubleVector.newVectorBuilder(
                randomBoolean() ? randomIntBetween(1, positionCount) : positionCount
            );
            IntStream.range(0, positionCount).mapToDouble(ii -> 1.0 / ii).forEach(blockBuilder::appendDouble);
            DoubleVector vector = blockBuilder.build();
            assertSingleValueDenseBlock(vector.asBlock());
        }
    }

    public void testConstantBooleanBlock() {
        for (int i = 0; i < 1000; i++) {
            int positionCount = randomIntBetween(1, 16 * 1024);
            boolean value = randomBoolean();
            BooleanBlock block;
            if (randomBoolean()) {
                block = BooleanBlock.newConstantBlockWith(value, positionCount);
            } else {
                block = new ConstantBooleanVector(value, positionCount).asBlock();
            }
            assertThat(positionCount, is(block.getPositionCount()));
            assertThat(block.getBoolean(0), is(value));
            assertThat(block.getBoolean(positionCount - 1), is(value));
            assertThat(block.getBoolean(randomPosition(positionCount)), is(value));
            assertSingleValueDenseBlock(block);
        }
    }

    public void testSingleValueSparseInt() {
        int positionCount = randomIntBetween(1, 16 * 1024);
        final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
        var blockBuilder = IntBlock.newBlockBuilder(builderEstimateSize);

        int[] values = new int[positionCount];
        for (int i = 0; i < positionCount; i++) {
            if (randomBoolean()) {
                values[i] = randomInt();
                blockBuilder.appendInt(values[i]);
            } else {
                blockBuilder.appendNull();
            }
        }
        IntBlock block = blockBuilder.build();

        assertThat(block.getPositionCount(), is(positionCount));
        assertThat(block.getTotalValueCount(), is(positionCount));
        int nullCount = 0;
        for (int i = 0; i < positionCount; i++) {
            if (block.isNull(i)) {
                nullCount++;
                // assertThat(block.getInt(i), is(0)); // Q: do we wanna allow access to the default value
            } else {
                assertThat(block.getInt(i), is(values[i]));
            }
        }
        assertThat(block.nullValuesCount(), is(nullCount));
        assertNull(block.asVector());
    }

    public void testSingleValueSparseLong() {
        int positionCount = randomIntBetween(1, 16 * 1024);
        final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
        var blockBuilder = LongBlock.newBlockBuilder(builderEstimateSize);

        long[] values = new long[positionCount];
        for (int i = 0; i < positionCount; i++) {
            if (randomBoolean()) {
                values[i] = randomLong();
                blockBuilder.appendLong(values[i]);
            } else {
                blockBuilder.appendNull();
            }
        }
        LongBlock block = blockBuilder.build();

        assertThat(block.getPositionCount(), is(positionCount));
        assertThat(block.getTotalValueCount(), is(positionCount));
        int nullCount = 0;
        for (int i = 0; i < positionCount; i++) {
            if (block.isNull(i)) {
                nullCount++;
                // assertThat(block.getInt(i), is(0)); // Q: do we wanna allow access to the default value
            } else {
                assertThat(block.getLong(i), is(values[i]));
            }
        }
        assertThat(block.nullValuesCount(), is(nullCount));
        assertNull(block.asVector());
    }

    public void testSingleValueSparseDouble() {
        int positionCount = randomIntBetween(1, 16 * 1024);
        final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
        var blockBuilder = DoubleBlock.newBlockBuilder(builderEstimateSize);

        double[] values = new double[positionCount];
        for (int i = 0; i < positionCount; i++) {
            if (randomBoolean()) {
                values[i] = randomDouble();
                blockBuilder.appendDouble(values[i]);
            } else {
                blockBuilder.appendNull();
            }
        }
        DoubleBlock block = blockBuilder.build();

        assertThat(block.getPositionCount(), is(positionCount));
        assertThat(block.getTotalValueCount(), is(positionCount));
        int nullCount = 0;
        for (int i = 0; i < positionCount; i++) {
            if (block.isNull(i)) {
                nullCount++;
                // assertThat(block.getDouble(i), is(0)); // Q: do we wanna allow access to the default value
            } else {
                assertThat(block.getDouble(i), is(values[i]));
            }
        }
        assertThat(block.nullValuesCount(), is(nullCount));
        assertNull(block.asVector());
    }

    public void testSingleValueSparseBoolean() {
        int positionCount = randomIntBetween(1, 16 * 1024);
        final int builderEstimateSize = randomBoolean() ? randomIntBetween(1, positionCount) : positionCount;
        var blockBuilder = BooleanBlock.newBlockBuilder(builderEstimateSize);

        boolean[] values = new boolean[positionCount];
        for (int i = 0; i < positionCount; i++) {
            if (randomBoolean()) {
                values[i] = randomBoolean();
                blockBuilder.appendBoolean(values[i]);
            } else {
                blockBuilder.appendNull();
            }
        }
        BooleanBlock block = blockBuilder.build();

        assertThat(block.getPositionCount(), is(positionCount));
        assertThat(block.getTotalValueCount(), is(positionCount));
        int nullCount = 0;
        for (int i = 0; i < positionCount; i++) {
            if (block.isNull(i)) {
                nullCount++;
            } else {
                assertThat(block.getBoolean(i), is(values[i]));
            }
        }
        assertThat(block.nullValuesCount(), is(nullCount));
        assertNull(block.asVector());
    }

    public void testNonDecreasingCalculatedTrue() {
        IntVector v = IntVector.newVectorBuilder(randomBoolean() ? 2 : 5).appendInt(1).appendInt(1).build();
        assertThat(v.isNonDecreasing(), is(true));
    }

    public void testNonDecreasingCalculatedFalse() {
        IntVector v = IntVector.newVectorBuilder(randomBoolean() ? 2 : 5).appendInt(1).appendInt(0).build();
        assertThat(v.isNonDecreasing(), is(false));
    }

    public void testNonDecreasingForSingleton() {
        IntVector v = IntVector.newVectorBuilder(1).appendInt(1).build();
        assertThat(v.isNonDecreasing(), is(true));
    }

    public void testNonDecreasingSet() {
        boolean hardSet = randomBoolean();
        IntVector.Builder b = IntVector.newVectorBuilder(2);
        b.appendInt(1).appendInt(2).setNonDecreasing(hardSet);
        assertThat(b.build().isNonDecreasing(), is(hardSet));
    }

    interface BlockBuilderFactory<B extends Block.Builder> {
        B create(int estimatedSize);
    }

    interface BlockProducer<B extends Block, BB extends Block.Builder> {
        B build(BB blockBuilder);
    }

    interface ValueAppender<BB extends Block.Builder, T> {
        void appendValue(BB blockBuilder, T value);
    }

    interface ValueSupplier<T> {
        T getValue(int position);
    }

    private static <B extends Block, BB extends Block.Builder, T> void assertNullValues(
        int positionCount,
        BlockBuilderFactory<BB> blockBuilderFactory,
        ValueAppender<BB, T> valueAppender,
        ValueSupplier<T> valueSupplier,
        BlockProducer<B, BB> blockProducer,
        BiConsumer<Integer, B> asserter
    ) {
        assertThat("test needs at least two positions", positionCount, greaterThan(1));
        int randomNullPosition = randomIntBetween(0, positionCount - 1);
        int randomNonNullPosition = randomValueOtherThan(randomNullPosition, () -> randomIntBetween(0, positionCount - 1));
        BitSet nullsMask = new BitSet(positionCount);
        nullsMask.set(randomNullPosition);

        var blockBuilder = blockBuilderFactory.create(positionCount);
        IntStream.range(0, positionCount).forEach(position -> {
            if (nullsMask.get(position)) {
                blockBuilder.appendNull();
            } else {
                valueAppender.appendValue(blockBuilder, valueSupplier.getValue(position));
            }
        });
        var block = blockProducer.build(blockBuilder);

        assertThat(positionCount, is(block.getPositionCount()));
        asserter.accept(randomNonNullPosition, block);
        assertTrue(block.isNull(randomNullPosition));
        assertFalse(block.isNull(randomNonNullPosition));
    }

    static int randomPosition(int positionCount) {
        return positionCount == 1 ? 0 : randomIntBetween(0, positionCount - 1);
    }

    static final Class<UnsupportedOperationException> UOE = UnsupportedOperationException.class;

}