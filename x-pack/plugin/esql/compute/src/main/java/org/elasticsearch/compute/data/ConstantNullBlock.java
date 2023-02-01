/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

/**
 * Block implementation representing a constant null value.
 */
final class ConstantNullBlock extends AbstractBlock {

    ConstantNullBlock(int positionCount) {
        super(positionCount);
    }

    @Override
    public Vector asVector() {
        return null;
    }

    @Override
    public boolean isNull(int position) {
        return true;
    }

    @Override
    public int nullValuesCount() {
        return getPositionCount();
    }

    @Override
    public boolean areAllValuesNull() {
        return true;
    }

    @Override
    public Block getRow(int position) {
        return null;
    }

    @Override
    public boolean mayHaveNulls() {
        return true;
    }

    @Override
    public ElementType elementType() {
        return ElementType.NULL;
    }

    @Override
    public Block filter(int... positions) {
        return new ConstantNullBlock(positions.length);
    }

    @Override
    public String toString() {
        return "ConstantNullBlock[positions=" + getPositionCount() + "]";
    }
}