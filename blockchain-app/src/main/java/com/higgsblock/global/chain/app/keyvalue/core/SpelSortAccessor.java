package com.higgsblock.global.chain.app.keyvalue.core;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.SortAccessor;
import org.springframework.data.keyvalue.core.SpelPropertyComparator;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;
import org.springframework.util.comparator.CompoundComparator;

import java.util.Comparator;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class SpelSortAccessor implements SortAccessor<Comparator<?>> {

    private final SpelExpressionParser parser;

    public SpelSortAccessor(SpelExpressionParser parser) {

        Assert.notNull(parser, "SpelExpressionParser must not be null!");
        this.parser = parser;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Comparator<?> resolve(KeyValueQuery<?> query) {

        if (query == null || query.getSort() == null) {
            return null;
        }

        CompoundComparator compoundComperator = new CompoundComparator();
        for (Order order : query.getSort()) {

            SpelPropertyComparator<?> spelSort = new SpelPropertyComparator(order.getProperty(), parser);

            if (Direction.DESC.equals(order.getDirection())) {

                spelSort.desc();

                if (order.getNullHandling() != null && !NullHandling.NATIVE.equals(order.getNullHandling())) {
                    spelSort = NullHandling.NULLS_FIRST.equals(order.getNullHandling()) ? spelSort.nullsFirst() : spelSort
                            .nullsLast();
                }
            }
            compoundComperator.addComparator(spelSort);
        }

        return compoundComperator;
    }
}
