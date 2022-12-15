/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.compute.aggregation;

import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.compute.Describable;
import org.elasticsearch.compute.Experimental;
import org.elasticsearch.compute.data.Block;
import org.elasticsearch.compute.data.Page;
import org.elasticsearch.core.Releasable;

@Experimental
public interface GroupingAggregatorFunction extends Releasable {

    void addRawInput(Block groupIdBlock, Page page);

    void addIntermediateInput(Block groupIdBlock, Block block);

    Block evaluateIntermediate();

    Block evaluateFinal();

    abstract class GroupingAggregatorFunctionFactory implements Describable {

        private final String name;

        GroupingAggregatorFunctionFactory(String name) {
            this.name = name;
        }

        public abstract GroupingAggregatorFunction build(BigArrays bigArrays, AggregatorMode mode, int inputChannel);

        @Override
        public String describe() {
            return name;
        }
    }

    GroupingAggregatorFunctionFactory avg = new GroupingAggregatorFunctionFactory("avg") {
        @Override
        public GroupingAggregatorFunction build(BigArrays bigArrays, AggregatorMode mode, int inputChannel) {
            if (mode.isInputPartial()) {
                return GroupingAvgAggregator.createIntermediate(bigArrays);
            } else {
                return GroupingAvgAggregator.create(bigArrays, inputChannel);
            }
        }
    };

    GroupingAggregatorFunctionFactory count = new GroupingAggregatorFunctionFactory("count") {
        @Override
        public GroupingAggregatorFunction build(BigArrays bigArrays, AggregatorMode mode, int inputChannel) {
            if (mode.isInputPartial()) {
                return GroupingCountAggregator.createIntermediate();
            } else {
                return GroupingCountAggregator.create(inputChannel);
            }
        }
    };

    GroupingAggregatorFunctionFactory min = new GroupingAggregatorFunctionFactory("min") {
        @Override
        public GroupingAggregatorFunction build(BigArrays bigArrays, AggregatorMode mode, int inputChannel) {
            if (mode.isInputPartial()) {
                return GroupingMinAggregator.createIntermediate();
            } else {
                return GroupingMinAggregator.create(inputChannel);
            }
        }
    };

    GroupingAggregatorFunctionFactory max = new GroupingAggregatorFunctionFactory("max") {
        @Override
        public GroupingAggregatorFunction build(BigArrays bigArrays, AggregatorMode mode, int inputChannel) {
            if (mode.isInputPartial()) {
                return GroupingMaxAggregator.createIntermediate();
            } else {
                return GroupingMaxAggregator.create(inputChannel);
            }
        }
    };

    GroupingAggregatorFunctionFactory sum = new GroupingAggregatorFunctionFactory("sum") {
        @Override
        public GroupingAggregatorFunction build(BigArrays bigArrays, AggregatorMode mode, int inputChannel) {
            if (mode.isInputPartial()) {
                return GroupingSumAggregator.createIntermediate();
            } else {
                return GroupingSumAggregator.create(inputChannel);
            }
        }
    };
}