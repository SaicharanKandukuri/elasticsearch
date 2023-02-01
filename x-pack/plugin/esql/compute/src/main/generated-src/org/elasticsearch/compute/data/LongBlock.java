/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

/**
 * Block that stores long values.
 * This class is generated. Do not edit it.
 */
public sealed interface LongBlock extends Block permits FilterLongBlock,LongArrayBlock,LongVectorBlock {

    /**
     * Retrieves the long value stored at the given value index.
     *
     * <p> Values for a given position are between getFirstValueIndex(position) (inclusive) and
     * getFirstValueIndex(position) + getValueCount(position) (exclusive).
     *
     * @param valueIndex the value index
     * @return the data value (as a long)
     */
    long getLong(int valueIndex);

    @Override
    LongVector asVector();

    @Override
    LongBlock getRow(int position);

    @Override
    LongBlock filter(int... positions);

    /**
     * Compares the given object with this block for equality. Returns {@code true} if and only if the
     * given object is a LongBlock, and both blocks are {@link #equals(LongBlock, LongBlock) equal}.
     */
    @Override
    boolean equals(Object obj);

    /** Returns the hash code of this block, as defined by {@link #hash(LongBlock)}. */
    @Override
    int hashCode();

    /**
     * Returns {@code true} if the given blocks are equal to each other, otherwise {@code false}.
     * Two blocks are considered equal if they have the same position count, and contain the same
     * values (including absent null values) in the same order. This definition ensures that the
     * equals method works properly across different implementations of the LongBlock interface.
     */
    static boolean equals(LongBlock block1, LongBlock block2) {
        final int positions = block1.getPositionCount();
        if (positions != block2.getPositionCount()) {
            return false;
        }
        for (int pos = 0; pos < positions; pos++) {
            if ((block1.isNull(pos) && block2.isNull(pos) == false) || (block2.isNull(pos) && block1.isNull(pos) == false)) {
                return false;
            }
            final int valueCount = block1.getValueCount(pos);
            if (valueCount != block2.getValueCount(pos)) {
                return false;
            }
            final int b1ValueIdx = block1.getFirstValueIndex(pos);
            final int b2ValueIdx = block2.getFirstValueIndex(pos);
            for (int valueIndex = 0; valueIndex < valueCount; valueIndex++) {
                if (block1.getLong(b1ValueIdx + valueIndex) != block2.getLong(b2ValueIdx + valueIndex)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Generates the hash code for the given block. The hash code is computed from the block's values.
     * This ensures that {@code block1.equals(block2)} implies that {@code block1.hashCode()==block2.hashCode()}
     * for any two blocks, {@code block1} and {@code block2}, as required by the general contract of
     * {@link Object#hashCode}.
     */
    static int hash(LongBlock block) {
        final int positions = block.getPositionCount();
        int result = 1;
        for (int pos = 0; pos < positions; pos++) {
            if (block.isNull(pos)) {
                result = 31 * result - 1;
            } else {
                final int valueCount = block.getValueCount(pos);
                result = 31 * result + valueCount;
                final int firstValueIdx = block.getFirstValueIndex(pos);
                for (int valueIndex = 0; valueIndex < valueCount; valueIndex++) {
                    long element = block.getLong(firstValueIdx + valueIndex);
                    result = 31 * result + (int) (element ^ (element >>> 32));
                }
            }
        }
        return result;
    }

    static Builder newBlockBuilder(int estimatedSize) {
        return new LongBlockBuilder(estimatedSize);
    }

    static LongBlock newConstantBlockWith(long value, int positions) {
        return new ConstantLongVector(value, positions).asBlock();
    }

    sealed interface Builder extends Block.Builder permits LongBlockBuilder {

        /**
         * Appends a long to the current entry.
         */
        Builder appendLong(long value);

        @Override
        Builder appendNull();

        @Override
        Builder beginPositionEntry();

        @Override
        Builder endPositionEntry();

        @Override
        LongBlock build();
    }
}