package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.keyvalue.core.query.IndexedKeyValueQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.data.keyvalue.core.SpelCriteria;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.Serializable;
import java.util.*;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class IndexedSpelQueryEngine<T extends KeyValueAdapter> extends QueryEngine<KeyValueAdapter, SpelCriteria, Comparator<?>> {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private static final SpelCriteriaAccessor CRITERIA_ACCESSOR = new SpelCriteriaAccessor(PARSER);
    private static final SpelSortAccessor SORT_ACCESSOR = new SpelSortAccessor(PARSER);

    public IndexedSpelQueryEngine() {
        super(CRITERIA_ACCESSOR, SORT_ACCESSOR);
    }

    @Override
    public Collection<?> execute(KeyValueQuery<?> query, Serializable keyspace) {
        if (query instanceof IndexedKeyValueQuery) {
            IndexedKeyValueQuery indexKeyValueQuery = (IndexedKeyValueQuery) query;
            if (indexKeyValueQuery.isIndexQuery()) {
                Collection<?> results = findByIndex(indexKeyValueQuery.getIndexParamNames(), indexKeyValueQuery.getIndexParams(), keyspace, indexKeyValueQuery.getClazz());
                SpelCriteria criteria = CRITERIA_ACCESSOR != null ? CRITERIA_ACCESSOR.resolve(query) : null;
                Comparator<?> sort = SORT_ACCESSOR != null ? SORT_ACCESSOR.resolve(query) : null;
                return sortAndFilterMatchingRange(results, criteria, sort, query.getOffset(), query.getRows());
            }
        }
        return super.execute(query, keyspace);
    }

    @Override
    public <T> Collection<T> execute(KeyValueQuery<?> query, Serializable keyspace, Class<T> type) {
        return (Collection<T>) execute(query, keyspace);
    }

    @Override
    public long count(KeyValueQuery<?> query, Serializable keyspace) {
        if (query instanceof IndexedKeyValueQuery) {
            IndexedKeyValueQuery indexKeyValueQuery = (IndexedKeyValueQuery) query;
            if (indexKeyValueQuery.isIndexQuery()) {
                Collection<?> results = findByIndex(indexKeyValueQuery.getIndexParamNames(), indexKeyValueQuery.getIndexParams(), keyspace, indexKeyValueQuery.getClazz());
                SpelCriteria criteria = CRITERIA_ACCESSOR != null ? CRITERIA_ACCESSOR.resolve(query) : null;
                return filterMatchingRange(results, criteria, -1, -1).size();
            }
        }
        return super.count(query, keyspace);
    }

    private Collection<?> findByIndex(List<String> indexQueryParamNames, List<Object> indexQueryParams, Serializable keyspace, Class<T> clazz) {
        Preconditions.checkArgument(getAdapter() instanceof IndexedKeyValueAdapter);
        IndexedKeyValueAdapter adapter = (IndexedKeyValueAdapter) getAdapter();

        String indexName = null;
        String opcode = null;
        Serializable param = null;
        Set<Serializable> ids = Sets.newHashSet();
        int indexes = indexQueryParamNames.size() / 2;

        for (int i = 0; i < indexes; i++) {
            opcode = indexQueryParamNames.get(2 * i);
            indexName = indexQueryParamNames.get(2 * i + 1);
            param = (Serializable) indexQueryParams.get(i);
            if (EntityClassInfos.isId(clazz, indexName)) {
                ids.add(param);
                continue;
            }
            if (!EntityClassInfos.isIndex(clazz, indexName)) {
                throw new RuntimeException("Unknown index");
            }
            switch (opcode) {
                case "and":
                    ids = and(ids, indexName, param, keyspace);
                    break;
                case "or":
                    ids = or(ids, indexName, param, keyspace);
                    break;
                case "in":
                    ids = in(ids, indexName, param, keyspace);
                    break;
                default:
                    throw new RuntimeException("Unsupported index query");
            }
        }

        Map<Serializable, Object> map = Maps.newHashMap();
        ids.forEach(id -> map.putIfAbsent(id, adapter.get(id, keyspace, clazz)));

        return map.values();
    }

    @Override
    public Collection<?> execute(SpelCriteria criteria, Comparator<?> sort, int offset, int rows, Serializable keyspace) {
        return sortAndFilterMatchingRange(getAdapter().getAllOf(keyspace), criteria, sort, offset, rows);
    }

    @Override
    public long count(SpelCriteria criteria, Serializable keyspace) {
        return filterMatchingRange(getAdapter().getAllOf(keyspace), criteria, -1, -1).size();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<?> sortAndFilterMatchingRange(Iterable<?> source, SpelCriteria criteria, Comparator sort, int offset,
                                               int rows) {

        List<?> tmp = IterableConverter.toList(source);
        if (sort != null) {
            Collections.sort(tmp, sort);
        }

        return filterMatchingRange(tmp, criteria, offset, rows);
    }

    private static <S> List<S> filterMatchingRange(Iterable<S> source, SpelCriteria criteria, int offset, int rows) {

        List<S> result = new ArrayList<S>();

        boolean compareOffsetAndRows = 0 < offset || 0 <= rows;
        int remainingRows = rows;
        int curPos = 0;

        for (S candidate : source) {

            boolean matches = criteria == null;

            if (!matches) {
                try {
                    matches = criteria.getExpression().getValue(criteria.getContext(), candidate, Boolean.class);
                } catch (SpelEvaluationException e) {
                    criteria.getContext().setVariable("it", candidate);
                    matches = criteria.getExpression().getValue(criteria.getContext()) == null ? false : criteria.getExpression()
                            .getValue(criteria.getContext(), Boolean.class);
                }
            }

            if (matches) {
                if (compareOffsetAndRows) {
                    if (curPos >= offset && rows > 0) {
                        result.add(candidate);
                        remainingRows--;
                        if (remainingRows <= 0) {
                            break;
                        }
                    }
                    curPos++;
                } else {
                    result.add(candidate);
                }
            }
        }

        return result;
    }

    private Set<Serializable> in(Set<Serializable> ids, String indexName, Object param, Serializable keyspace) {
        Set<Serializable> indexes = Sets.newHashSet();
        if (param instanceof Serializable[]) {
            CollectionUtils.addAll(indexes, (Object[]) param);
        } else if (param instanceof Collection) {
            indexes.addAll((Collection<? extends Serializable>) param);
        } else {
            indexes.add((Serializable) param);
        }


        Set<Serializable> result = Sets.newHashSet(ids);
        for (Serializable index : indexes) {
            result = or(ids, indexName, index, keyspace);
        }
        return result;
    }

    private Set<Serializable> and(Set<Serializable> ids, String indexName, Serializable index, Serializable keyspace) {
        return Sets.intersection(ids, Sets.newHashSet(findIdsByIndex(indexName, index, keyspace))).immutableCopy();
    }

    private Set<Serializable> or(Set<Serializable> ids, String indexName, Serializable index, Serializable keyspace) {
        return Sets.union(ids, Sets.newHashSet(findIdsByIndex(indexName, index, keyspace))).immutableCopy();
    }

    private Collection<Serializable> findIdsByIndex(String indexName, Serializable index, Serializable keyspace) {
        return ((IndexedKeyValueAdapter) getAdapter()).findIndex(indexName, index, keyspace);
    }
}
