package com.higgsblock.global.chain.app.keyvalue.core.query;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-09-04
 */
@Data
public class IndexedKeyValueQuery<T> extends KeyValueQuery<T> {

    private List<String> indexParamNames;
    private List<Object> indexParams;
    private Class clazz;

    public IndexedKeyValueQuery() {
    }

    public IndexedKeyValueQuery(T criteria) {
        super(criteria);
    }

    public IndexedKeyValueQuery(Sort sort) {
        super(sort);
    }

    public IndexedKeyValueQuery(KeyValueQuery<T> query) {
        super(query.getCriteria());

        setOffset(query.getOffset());
        setRows(query.getRows());
        setSort(query.getSort());
    }

    public boolean isIndexQuery() {
        return CollectionUtils.isNotEmpty(indexParamNames)
                && CollectionUtils.isNotEmpty(indexParams)
                && indexParamNames.size() / 2 <= indexParams.size()
                && null != clazz;
    }
}
