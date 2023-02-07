/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.session;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.xpack.ql.expression.Attribute;

import java.util.List;

public interface LocalExecutable {

    List<Attribute> output();

    void execute(EsqlSession session, ActionListener<Result> listener);
}