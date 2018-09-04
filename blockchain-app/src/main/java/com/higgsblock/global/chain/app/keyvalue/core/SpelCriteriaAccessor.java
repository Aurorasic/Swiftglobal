package com.higgsblock.global.chain.app.keyvalue.core;

import org.springframework.data.keyvalue.core.CriteriaAccessor;
import org.springframework.data.keyvalue.core.SpelCriteria;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class SpelCriteriaAccessor implements CriteriaAccessor<SpelCriteria> {

    private final SpelExpressionParser parser;

    public SpelCriteriaAccessor(SpelExpressionParser parser) {

        Assert.notNull(parser, "SpelExpressionParser must not be null!");

        this.parser = parser;
    }

    @Override
    public SpelCriteria resolve(KeyValueQuery<?> query) {

        if (query.getCriteria() == null) {
            return null;
        }

        if (query.getCriteria() instanceof SpelExpression) {
            return new SpelCriteria((SpelExpression) query.getCriteria());
        }

        if (query.getCriteria() instanceof String) {
            return new SpelCriteria(parser.parseRaw((String) query.getCriteria()));
        }

        if (query.getCriteria() instanceof SpelCriteria) {
            return (SpelCriteria) query.getCriteria();
        }

        throw new IllegalArgumentException("Cannot create SpelCriteria for " + query.getCriteria());
    }
}
