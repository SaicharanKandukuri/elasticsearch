/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.compute.data;

import java.util.Arrays;

final class IntBlockBuilder extends AbstractBlockBuilder {

    private int[] values;

    IntBlockBuilder(int estimatedSize) {
        values = new int[Math.max(estimatedSize, 2)];
    }

    @Override
    public BlockBuilder appendInt(int value) {
        ensureCapacity();
        values[valueCount] = value;
        hasNonNullValue = true;
        valueCount++;
        updatePosition();
        return this;
    }

    @Override
    protected int valuesLength() {
        return values.length;
    }

    @Override
    protected void growValuesArray(int newSize) {
        values = Arrays.copyOf(values, newSize);
    }

    @Override
    public Block build() {
        if (positionEntryIsOpen) {
            endPositionEntry();
        }
        if (hasNonNullValue == false) {
            return new ConstantNullBlock(positionCount);
        } else if (positionCount == 1) {
            return new VectorBlock(new ConstantIntVector(values[0], 1));
        } else {
            // TODO: may wanna trim the array, if there N% unused tail space
            if (isDense() && singleValued()) {
                return new VectorBlock(new IntVector(values, positionCount));
            } else {
                if (firstValueIndexes != null) {
                    firstValueIndexes[positionCount] = valueCount;  // hack
                }
                return new IntBlock(values, positionCount, firstValueIndexes, nullsMask);
            }
        }
    }
}