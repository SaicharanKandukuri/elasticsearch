/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.aggregation;

import org.elasticsearch.common.Randomness;
import org.elasticsearch.compute.data.Block;
import org.elasticsearch.compute.data.DoubleBlock;
import org.elasticsearch.compute.operator.SequenceDoubleBlockSourceOperator;
import org.elasticsearch.compute.operator.SourceOperator;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class MedianDoubleAggregatorFunctionTests extends AggregatorFunctionTestCase {

    @Override
    protected SourceOperator simpleInput(int end) {
        List<Double> values = Arrays.asList(1.2, 1.25, 2.0, 2.0, 4.3, 6.0, 9.0);
        Randomness.shuffle(values);
        return new SequenceDoubleBlockSourceOperator(values);
    }

    @Override
    protected AggregatorFunction.Factory aggregatorFunction() {
        return AggregatorFunction.MEDIAN_DOUBLES;
    }

    @Override
    protected String expectedDescriptionOfAggregator() {
        return "median of doubles";
    }

    @Override
    protected void assertSimpleOutput(List<Block> input, Block result) {
        assertThat(((DoubleBlock) result).getDouble(0), equalTo(2.0));
    }
}