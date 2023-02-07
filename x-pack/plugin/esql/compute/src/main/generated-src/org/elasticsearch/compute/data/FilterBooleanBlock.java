/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

/**
 * Filter block for BooleanBlocks.
 * This class is generated. Do not edit it.
 */
final class FilterBooleanBlock extends AbstractFilterBlock implements BooleanBlock {

    private final BooleanBlock block;

    FilterBooleanBlock(BooleanBlock block, int... positions) {
        super(block, positions);
        this.block = block;
    }

    @Override
    public BooleanVector asVector() {
        return null;
    }

    @Override
    public boolean getBoolean(int valueIndex) {
        return block.getBoolean(mapPosition(valueIndex));
    }

    @Override
    public ElementType elementType() {
        return ElementType.BOOLEAN;
    }

    @Override
    public BooleanBlock getRow(int position) {
        return filter(position);
    }

    @Override
    public BooleanBlock filter(int... positions) {
        return new FilterBooleanBlock(this, positions);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BooleanBlock that) {
            return BooleanBlock.equals(this, that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return BooleanBlock.hash(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[block=" + block + "]";
    }
}