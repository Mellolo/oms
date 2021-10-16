package com.hengtiansoft.strategy.dao;

import com.hengtiansoft.strategy.model.Strategy;
import com.hengtiansoft.strategy.utils.ESUtils;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//@Component
public class EsTemplate {

    @Autowired
    @Qualifier("client")
    RestHighLevelClient restHighLevelClient;

    //查询
    public List<Strategy> search() {
        // user_city 完全匹配 Beijing 且 (2020-6-1 <= user_time <= 2020-6-2)
        QueryBuilder queryBuilder = QueryBuilders.boolQuery();
                //.must(QueryBuilders.termQuery("userId", "rtt"));
                //.must(QueryBuilders.rangeQuery("account_id").gte("2020-6-1 00:00:00"))
                //.must(QueryBuilders.rangeQuery("user_time").lte("2020-6-2 00:00:00"));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        // 按 user_time 升序排序
        //searchSourceBuilder.sort("user_time", SortOrder.ASC);
        // 设置返回数量
        searchSourceBuilder.size(1000);

        SearchResponse searchResponse = null;
        try {
            searchResponse = ESUtils.getInstance(restHighLevelClient)
                    .searchBySearchSourceBuilde("strategies", searchSourceBuilder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SearchHit[] hitsArr = searchResponse.getHits().getHits();

        List<Strategy> strategyList = new ArrayList<>();
        for (SearchHit searchHit : hitsArr) {
            Strategy strategy = new Strategy();

            JSONObject source = JSONObject.parseObject(searchHit.getSourceAsString());
            strategy.setUserId(source.getString("userId"));
            strategy.setAccountId(source.getString("accountId"));
            strategy.setCode(source.getString("code"));
            strategyList.add(strategy);
        }
        return strategyList;
    }

    //批量插入
    public BulkResponse bulkInsert(List<Strategy> strategyList) {
        List<String> jsonList = new ArrayList<>();

        for (Strategy strategy : strategyList) {
            // User 转为 Json字符串
            jsonList.add(JSONObject.toJSONString(strategy));
        }
        return ESUtils.getInstance(restHighLevelClient).bulkInsert("strategies",jsonList);
    }
}
