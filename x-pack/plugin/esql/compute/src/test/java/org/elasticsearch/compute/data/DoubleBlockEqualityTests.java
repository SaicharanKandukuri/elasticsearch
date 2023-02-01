/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

import org.elasticsearch.test.ESTestCase;

import java.util.BitSet;
import java.util.List;

public class DoubleBlockEqualityTests extends ESTestCase {

    public void testEmptyVector() {
        // all these "empty" vectors should be equivalent
        List<DoubleVector> vectors = List.of(
            new DoubleArrayVector(new double[] {}, 0),
            new DoubleArrayVector(new double[] { 0 }, 0),
            DoubleBlock.newConstantBlockWith(0, 0).asVector(),
            DoubleBlock.newConstantBlockWith(0, 0).filter().asVector(),
            DoubleBlock.newBlockBuilder(0).build().asVector(),
            DoubleBlock.newBlockBuilder(0).appendDouble(1).build().asVector().filter()
        );
        assertAllEquals(vectors);
    }

    public void testEmptyBlock() {
        // all these "empty" vectors should be equivalent
        List<DoubleBlock> blocks = List.of(
            new DoubleArrayBlock(new double[] {}, 0, new int[] {}, BitSet.valueOf(new byte[] { 0b00 })),
            new DoubleArrayBlock(new double[] { 0 }, 0, new int[] {}, BitSet.valueOf(new byte[] { 0b00 })),
            DoubleBlock.newConstantBlockWith(0, 0),
            DoubleBlock.newBlockBuilder(0).build(),
            DoubleBlock.newBlockBuilder(0).appendDouble(1).build().filter(),
            DoubleBlock.newBlockBuilder(0).appendNull().build().filter()
        );
        assertAllEquals(blocks);
    }

    public void testVectorEquality() {
        // all these vectors should be equivalent
        List<DoubleVector> vectors = List.of(
            new DoubleArrayVector(new double[] { 1, 2, 3 }, 3),
            new DoubleArrayVector(new double[] { 1, 2, 3 }, 3).asBlock().asVector(),
            new DoubleArrayVector(new double[] { 1, 2, 3, 4 }, 3),
            new DoubleArrayVector(new double[] { 1, 2, 3 }, 3).filter(0, 1, 2),
            new DoubleArrayVector(new double[] { 1, 2, 3, 4 }, 4).filter(0, 1, 2),
            new DoubleArrayVector(new double[] { 0, 1, 2, 3 }, 4).filter(1, 2, 3),
            new DoubleArrayVector(new double[] { 1, 4, 2, 3 }, 4).filter(0, 2, 3),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(2).appendDouble(3).build().asVector(),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(2).appendDouble(3).build().asVector().filter(0, 1, 2),
            DoubleBlock.newBlockBuilder(3)
                .appendDouble(1)
                .appendDouble(4)
                .appendDouble(2)
                .appendDouble(3)
                .build()
                .filter(0, 2, 3)
                .asVector(),
            DoubleBlock.newBlockBuilder(3)
                .appendDouble(1)
                .appendDouble(4)
                .appendDouble(2)
                .appendDouble(3)
                .build()
                .asVector()
                .filter(0, 2, 3)
        );
        assertAllEquals(vectors);

        // all these constant-like vectors should be equivalent
        List<DoubleVector> moreVectors = List.of(
            new DoubleArrayVector(new double[] { 1, 1, 1 }, 3),
            new DoubleArrayVector(new double[] { 1, 1, 1 }, 3).asBlock().asVector(),
            new DoubleArrayVector(new double[] { 1, 1, 1, 1 }, 3),
            new DoubleArrayVector(new double[] { 1, 1, 1 }, 3).filter(0, 1, 2),
            new DoubleArrayVector(new double[] { 1, 1, 1, 4 }, 4).filter(0, 1, 2),
            new DoubleArrayVector(new double[] { 3, 1, 1, 1 }, 4).filter(1, 2, 3),
            new DoubleArrayVector(new double[] { 1, 4, 1, 1 }, 4).filter(0, 2, 3),
            DoubleBlock.newConstantBlockWith(1, 3).asVector(),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(1).appendDouble(1).build().asVector(),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(1).appendDouble(1).build().asVector().filter(0, 1, 2),
            DoubleBlock.newBlockBuilder(3)
                .appendDouble(1)
                .appendDouble(4)
                .appendDouble(1)
                .appendDouble(1)
                .build()
                .filter(0, 2, 3)
                .asVector(),
            DoubleBlock.newBlockBuilder(3)
                .appendDouble(1)
                .appendDouble(4)
                .appendDouble(1)
                .appendDouble(1)
                .build()
                .asVector()
                .filter(0, 2, 3)
        );
        assertAllEquals(moreVectors);
    }

    public void testBlockEquality() {
        // all these blocks should be equivalent
        List<DoubleBlock> blocks = List.of(
            new DoubleArrayVector(new double[] { 1, 2, 3 }, 3).asBlock(),
            new DoubleArrayBlock(new double[] { 1, 2, 3 }, 3, new int[] { 0, 1, 2, 3 }, BitSet.valueOf(new byte[] { 0b000 })),
            new DoubleArrayBlock(new double[] { 1, 2, 3, 4 }, 3, new int[] { 0, 1, 2, 3 }, BitSet.valueOf(new byte[] { 0b1000 })),
            new DoubleArrayVector(new double[] { 1, 2, 3 }, 3).filter(0, 1, 2).asBlock(),
            new DoubleArrayVector(new double[] { 1, 2, 3, 4 }, 3).filter(0, 1, 2).asBlock(),
            new DoubleArrayVector(new double[] { 1, 2, 3, 4 }, 4).filter(0, 1, 2).asBlock(),
            new DoubleArrayVector(new double[] { 1, 2, 4, 3 }, 4).filter(0, 1, 3).asBlock(),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(2).appendDouble(3).build(),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(2).appendDouble(3).build().filter(0, 1, 2),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(4).appendDouble(2).appendDouble(3).build().filter(0, 2, 3),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendNull().appendDouble(2).appendDouble(3).build().filter(0, 2, 3)
        );
        assertAllEquals(blocks);

        // all these constant-like blocks should be equivalent
        List<DoubleBlock> moreBlocks = List.of(
            new DoubleArrayVector(new double[] { 9, 9 }, 2).asBlock(),
            new DoubleArrayBlock(new double[] { 9, 9 }, 2, new int[] { 0, 1, 2 }, BitSet.valueOf(new byte[] { 0b000 })),
            new DoubleArrayBlock(new double[] { 9, 9, 4 }, 2, new int[] { 0, 1, 2 }, BitSet.valueOf(new byte[] { 0b100 })),
            new DoubleArrayVector(new double[] { 9, 9 }, 2).filter(0, 1).asBlock(),
            new DoubleArrayVector(new double[] { 9, 9, 4 }, 2).filter(0, 1).asBlock(),
            new DoubleArrayVector(new double[] { 9, 9, 4 }, 3).filter(0, 1).asBlock(),
            new DoubleArrayVector(new double[] { 9, 4, 9 }, 3).filter(0, 2).asBlock(),
            DoubleBlock.newConstantBlockWith(9, 2),
            DoubleBlock.newBlockBuilder(2).appendDouble(9).appendDouble(9).build(),
            DoubleBlock.newBlockBuilder(2).appendDouble(9).appendDouble(9).build().filter(0, 1),
            DoubleBlock.newBlockBuilder(2).appendDouble(9).appendDouble(4).appendDouble(9).build().filter(0, 2),
            DoubleBlock.newBlockBuilder(2).appendDouble(9).appendNull().appendDouble(9).build().filter(0, 2)
        );
        assertAllEquals(moreBlocks);
    }

    public void testVectorInequality() {
        // all these vectors should NOT be equivalent
        List<DoubleVector> notEqualVectors = List.of(
            new DoubleArrayVector(new double[] { 1 }, 1),
            new DoubleArrayVector(new double[] { 9 }, 1),
            new DoubleArrayVector(new double[] { 1, 2 }, 2),
            new DoubleArrayVector(new double[] { 1, 2, 3 }, 3),
            new DoubleArrayVector(new double[] { 1, 2, 4 }, 3),
            DoubleBlock.newConstantBlockWith(9, 2).asVector(),
            DoubleBlock.newBlockBuilder(2).appendDouble(1).appendDouble(2).build().asVector().filter(1),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(2).appendDouble(5).build().asVector(),
            DoubleBlock.newBlockBuilder(1).appendDouble(1).appendDouble(2).appendDouble(3).appendDouble(4).build().asVector()
        );
        assertAllNotEquals(notEqualVectors);
    }

    public void testBlockInequality() {
        // all these blocks should NOT be equivalent
        List<DoubleBlock> notEqualBlocks = List.of(
            new DoubleArrayVector(new double[] { 1 }, 1).asBlock(),
            new DoubleArrayVector(new double[] { 9 }, 1).asBlock(),
            new DoubleArrayVector(new double[] { 1, 2 }, 2).asBlock(),
            new DoubleArrayVector(new double[] { 1, 2, 3 }, 3).asBlock(),
            new DoubleArrayVector(new double[] { 1, 2, 4 }, 3).asBlock(),
            DoubleBlock.newConstantBlockWith(9, 2),
            DoubleBlock.newBlockBuilder(2).appendDouble(1).appendDouble(2).build().filter(1),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).appendDouble(2).appendDouble(5).build(),
            DoubleBlock.newBlockBuilder(1).appendDouble(1).appendDouble(2).appendDouble(3).appendDouble(4).build(),
            DoubleBlock.newBlockBuilder(1).appendDouble(1).appendNull().build(),
            DoubleBlock.newBlockBuilder(1).appendDouble(1).appendNull().appendDouble(3).build(),
            DoubleBlock.newBlockBuilder(1).appendDouble(1).appendDouble(3).build(),
            DoubleBlock.newBlockBuilder(3).appendDouble(1).beginPositionEntry().appendDouble(2).appendDouble(3).build()
        );
        assertAllNotEquals(notEqualBlocks);
    }

    static void assertAllEquals(List<?> objs) {
        for (Object obj1 : objs) {
            for (Object obj2 : objs) {
                assertEquals(obj1, obj2);
                // equal objects must generate the same hash code
                assertEquals(obj1.hashCode(), obj2.hashCode());
            }
        }
    }

    static void assertAllNotEquals(List<?> objs) {
        for (Object obj1 : objs) {
            for (Object obj2 : objs) {
                if (obj1 == obj2) {
                    continue; // skip self
                }
                assertNotEquals(obj1, obj2);
                // unequal objects SHOULD generate the different hash code
                assertNotEquals(obj1.hashCode(), obj2.hashCode());
            }
        }
    }
}