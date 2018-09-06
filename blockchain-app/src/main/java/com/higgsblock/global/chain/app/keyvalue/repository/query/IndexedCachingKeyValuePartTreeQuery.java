package com.higgsblock.global.chain.app.keyvalue.repository.query;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.core.query.IndexedKeyValueQuery;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class IndexedCachingKeyValuePartTreeQuery extends KeyValuePartTreeQuery {

    private static final Pattern DELETE_PREFIX_TEMPLATE = Pattern.compile("^(delete|remove)((\\p{Lu}.*?))??By");

    private QueryMethod queryMethod;
    private KeyValueOperations keyValueOperations;

    public IndexedCachingKeyValuePartTreeQuery(QueryMethod queryMethod, EvaluationContextProvider evaluationContextProvider,
                                               KeyValueOperations keyValueOperations, Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
        super(queryMethod, evaluationContextProvider, keyValueOperations, queryCreator);
        this.queryMethod = queryMethod;
        this.keyValueOperations = keyValueOperations;
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

    @Override
    protected Object doExecute(Object[] parameters, KeyValueQuery<?> query) {
        String methodName = queryMethod.getName();
        Matcher deleteMatcher = DELETE_PREFIX_TEMPLATE.matcher(methodName);
        if (deleteMatcher.find()) {
            Iterable<?> objects = this.keyValueOperations.find(query, queryMethod.getEntityInformation().getJavaType());
            CollectionUtils.forAllDo(Lists.newLinkedList(objects), obj -> keyValueOperations.delete(obj));
            return objects;
        }

        return super.doExecute(parameters, query);
    }
}
