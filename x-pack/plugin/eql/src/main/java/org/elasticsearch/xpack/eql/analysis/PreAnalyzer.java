/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.eql.analysis;

import org.elasticsearch.xpack.ql.common.Failure;
import org.elasticsearch.xpack.ql.index.IndexResolution;
import org.elasticsearch.xpack.ql.plan.logical.EsRelation;
import org.elasticsearch.xpack.ql.plan.logical.LogicalPlan;
import org.elasticsearch.xpack.ql.plan.logical.UnresolvedRelation;

import java.util.Collections;

public class PreAnalyzer {

    public LogicalPlan preAnalyze(LogicalPlan plan, IndexResolution indices) {
        // wrap a potential index_not_found_exception with a VerificationException (expected by client)
        if (indices.isValid() == false) {
            throw new VerificationException(Collections.singletonList(Failure.fail(plan, indices.toString())));
        }
        if (plan.analyzed() == false) {
            final EsRelation esRelation = new EsRelation(plan.source(), indices.get(), false);
            // FIXME: includeFrozen needs to be set already
            plan = plan.transformUp(r -> esRelation, UnresolvedRelation.class);
            plan.forEachUp(LogicalPlan::setPreAnalyzed);
        }
        return plan;
    }
}
