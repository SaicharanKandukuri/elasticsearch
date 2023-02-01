/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

$if(BytesRef)$
import org.apache.lucene.util.BytesRef;
$endif$

/**
 * Filter block for $Type$Blocks.
 * This class is generated. Do not edit it.
 */
final class Filter$Type$Block extends AbstractFilterBlock implements $Type$Block {

    private final $Type$Block block;

    Filter$Type$Block($Type$Block block, int... positions) {
        super(block, positions);
        this.block = block;
    }

    @Override
    public $Type$Vector asVector() {
        return null;
    }

    @Override
$if(BytesRef)$
    public BytesRef getBytesRef(int valueIndex, BytesRef dest) {
        return block.getBytesRef(mapPosition(valueIndex), dest);
$else$
    public $type$ get$Type$(int valueIndex) {
        return block.get$Type$(mapPosition(valueIndex));
$endif$
    }

$if(int)$
    @Override
    public LongBlock asLongBlock() {
        return new FilterLongBlock(block.asLongBlock(), positions);
    }
$endif$

    @Override
    public ElementType elementType() {
        return ElementType.$TYPE$;
    }

    @Override
    public $Type$Block getRow(int position) {
        return filter(position);
    }

    @Override
    public $Type$Block filter(int... positions) {
        return new Filter$Type$Block(this, positions);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof $Type$Block that) {
            return $Type$Block.equals(this, that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return $Type$Block.hash(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[block=" + block + "]";
    }
}