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
 * Filter vector for $Type$Vectors.
 * This class is generated. Do not edit it.
 */
public final class Filter$Type$Vector extends AbstractFilterVector implements $Type$Vector {

    private final $Type$Vector vector;

    Filter$Type$Vector($Type$Vector vector, int... positions) {
        super(positions);
        this.vector = vector;
    }

    @Override
$if(BytesRef)$
    public BytesRef getBytesRef(int position, BytesRef dest) {
        return vector.getBytesRef(mapPosition(position), dest);
$else$
    public $type$ get$Type$(int position) {
        return vector.get$Type$(mapPosition(position));
$endif$
    }

    @Override
    public $Type$Block asBlock() {
        return new $Type$VectorBlock(this);
    }

    @Override
    public ElementType elementType() {
        return ElementType.$TYPE$;
    }

    @Override
    public boolean isConstant() {
        return vector.isConstant();
    }

    @Override
    public $Type$Vector filter(int... positions) {
        return new Filter$Type$Vector(this, positions);
    }

$if(int)$
    @Override
    public boolean isNonDecreasing() {
        return vector.isNonDecreasing();
    }
$endif$

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof $Type$Vector that) {
            return $Type$Vector.equals(this, that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return $Type$Vector.hash(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[vector=" + vector + "]";
    }
}