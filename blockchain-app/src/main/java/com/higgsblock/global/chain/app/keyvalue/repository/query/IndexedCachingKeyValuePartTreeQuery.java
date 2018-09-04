package com.higgsblock.global.chain.app.keyvalue.repository.query;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.core.query.IndexedKeyValueQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.repository.query.*;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class IndexedCachingKeyValuePartTreeQuery extends KeyValuePartTreeQuery {

    private QueryMethod queryMethod;

    public IndexedCachingKeyValuePartTreeQuery(QueryMethod queryMethod, EvaluationContextProvider evaluationContextProvider,
                                               KeyValueOperations keyValueOperations, Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
        super(queryMethod, evaluationContextProvider, keyValueOperations, queryCreator);
        this.queryMethod = queryMethod;
    }

    @Override
    public Object execute(Object[] parameters) {
        ParameterAccessor accessor = new ParametersParameterAccessor(getQueryMethod().getParameters(), parameters);
        ResultProcessor processor = queryMethod.getResultProcessor().withDynamicProjection(accessor);

        KeyValueQuery<?> query = new IndexedKeyValueQuery(prepareQuery(parameters));
        Field field = ReflectionUtils.findField(queryMethod.getClass(), "method", Method.class);
        field.setAccessible(true);
        Method method = (Method) ReflectionUtils.getField(field, queryMethod);
        IndexQuery indexQuery = AnnotatedElementUtils.findMergedAnnotation(method, IndexQuery.class);
        boolean isIndexQuery = null != indexQuery;
        if (isIndexQuery) {
            String queryString = indexQuery.value().trim();

            if (StringUtils.isNotEmpty(queryString) && !StringUtils.startsWithIgnoreCase(queryString, "in")) {
                queryString = "or " + queryString;
            }
            List<String> paramNames = Splitter.on(" ").splitToList(queryString).stream()
                    .filter(StringUtils::isNotEmpty)
                    .map(StringUtils::uncapitalize)
                    .collect(Collectors.toList());

            IndexedKeyValueQuery indexKeyValueQuery = new IndexedKeyValueQuery(query);
            indexKeyValueQuery.setIndexParamNames(paramNames);
            indexKeyValueQuery.setIndexParams(Lists.newArrayList(parameters));
            indexKeyValueQuery.setClazz(queryMethod.getEntityInformation().getJavaType());
            query = indexKeyValueQuery;
        }

        return processor.processResult(doExecute(parameters, query));
    }

}
