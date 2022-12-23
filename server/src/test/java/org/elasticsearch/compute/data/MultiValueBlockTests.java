/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.compute.data;

import org.elasticsearch.test.ESTestCase;

import static org.elasticsearch.test.hamcrest.OptionalMatchers.isEmpty;
import static org.hamcrest.Matchers.is;

public class MultiValueBlockTests extends ESTestCase {

    public void testIntBlockTrivial1() {
        BlockBuilder blockBuilder = BlockBuilder.newIntBlockBuilder(4);
        blockBuilder.appendInt(10);
        blockBuilder.beginPositionEntry();
        blockBuilder.appendInt(21);
        blockBuilder.appendInt(22);
        blockBuilder.appendInt(23);
        Block block = blockBuilder.build();

        // expect two positions
        assertThat(block.getPositionCount(), is(2));

        // expect four values
        assertThat(block.getTotalValueCount(), is(4));

        // assert first position
        assertThat(block.getValueCount(0), is(1));
        assertThat(block.getFirstValueIndex(0), is(0));
        assertThat(block.getInt(block.getFirstValueIndex(0)), is(10));

        // assert second position
        assertThat(block.getValueCount(1), is(3));
        assertThat(block.getFirstValueIndex(1), is(1));
        int expectedValue = 21;
        for (int i = 0; i < block.getValueCount(1); i++) {
            assertThat(block.getInt(block.getFirstValueIndex(1) + i), is(expectedValue));
            expectedValue++;
        }

        // cannot get a Vector view
        assertThat(block.asVector(), isEmpty());
    }

    public void testIntBlockTrivial() {
        BlockBuilder blockBuilder = BlockBuilder.newIntBlockBuilder(10);
        blockBuilder.appendInt(1);
        blockBuilder.beginPositionEntry();
        blockBuilder.appendInt(21);
        blockBuilder.appendInt(22);
        blockBuilder.appendInt(23);
        blockBuilder.endPositionEntry();
        blockBuilder.beginPositionEntry();
        blockBuilder.appendInt(31);
        blockBuilder.appendInt(32);
        blockBuilder.endPositionEntry();
        blockBuilder.beginPositionEntry();
        blockBuilder.appendInt(41);
        blockBuilder.endPositionEntry();
        Block block = blockBuilder.build();

        assertThat(block.getPositionCount(), is(4));
        assertThat(block.getFirstValueIndex(0), is(0));
        assertThat(block.getValueCount(0), is(1));
        assertThat(block.getInt(block.getFirstValueIndex(0)), is(1));
        assertThat(block.asVector(), isEmpty());
    }

    public void testIntBlock() {
        final int totalLen = randomIntBetween(1, 1000000);
        final int startLen = randomIntBetween(1, randomBoolean() ? 1000 : totalLen);
        // IntArray array = bigArrays.newIntArray(startLen, randomBoolean());
        // int[] ref = new int[totalLen];
        // for (int i = 0; i < totalLen; ++i) {
        // ref[i] = randomInt();
        // array = bigArrays.grow(array, i + 1);
        // array.set(i, ref[i]);
        // }
        // for (int i = 0; i < totalLen; ++i) {
        // assertEquals(ref[i], array.get(i));
        // }
        // array.close();
    }
}