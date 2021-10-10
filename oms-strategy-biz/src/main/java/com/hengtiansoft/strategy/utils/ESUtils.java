package com.hengtiansoft.strategy.utils;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.List;

public class ESUtils {

    private static RestHighLevelClient restHighLevelClient;

    private static volatile ESUtils eSUtils;
    private ESUtils(){}

    public static  ESUtils getInstance(RestHighLevelClient restclient) {
        restHighLevelClient = restclient;

        if (eSUtils == null) {
            synchronized (ESUtils.class) {
                if (eSUtils == null) {
                    eSUtils = new ESUtils();
                }
            }
        }
        return eSUtils;
    }

    /**
     * 查询
     * @param index 索引
     * @param searchSourceBuilder
     */
    public SearchResponse searchBySearchSourceBuilde(String index,
                                                     SearchSourceBuilder searchSourceBuilder) throws IOException {
        // 组装SearchRequest请求
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);
        // 同步获取SearchResponse结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        return searchResponse;
    }

    /**
     * 批量插入  批量修改删除原理相同，也可混合 只需在 bulkRequest.add 不同请求即可
     * @param jsonStrList  待插入集合 注：每个String元素需为json字符串
     * @param index 索引
     * @return
     */
    public BulkResponse bulkInsert(String index, List<String> jsonStrList) {
        BulkRequest bulkRequest = new BulkRequest();

        for (String jsonStr : jsonStrList) {
            IndexRequest indexRequest = new IndexRequest(index);
            indexRequest.source(jsonStr, XContentType.JSON);

            bulkRequest.add(indexRequest); // 加入到批量请求bulk
        }

        BulkResponse bulkResponse = null;
        try {
            bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bulkResponse;
    }
}
