package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
    @Autowired
    private RestHighLevelClient client;

    /**
     * 根据关键字搜索酒店数据
     *
     * @param params
     * @return
     */
    @Override
    public PageResult search(RequestParams params) {
        try {
            //     1. 准备Request
            SearchRequest request = new SearchRequest("hotel");
            //     2. 准备DSL
            //     2.1 query
            // 封装一个函数来组合多个查询条件
            buildBasicQuery(params, request);

            //     2.2 分页 from
            Integer page = params.getPage();
            Integer size = params.getSize();
            request.source().from((page - 1) * size).size(size);

            // 2.3 排序 sort
            String location = params.getLocation();
            // 由于在国外 所以 fake 了一个国内的地址
            location = "23.1, 120.385766";
            if (location != null && !location.isEmpty()) {
                request.source().sort(
                        SortBuilders.geoDistanceSort("location", new GeoPoint(location))
                                .order(SortOrder.ASC)
                                .unit(DistanceUnit.KILOMETERS)
                );
            }

            //     3. 发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //     4. 解析响应
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对搜索结果做聚合，限定显示的城市、星级、品牌列表
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, List<String>> filters(RequestParams params) {
        try {
            // 1.准备Request
            SearchRequest request = new SearchRequest("hotel");
            // 2.准备DSL
            // 2.1.query
            buildBasicQuery(params, request);
            // 2.2.设置size
            request.source().size(0);
            // 2.3.聚合
            buildAggregation(request);
            // 3.发出请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 4.解析结果
            Map<String, List<String>> result = new HashMap<>();
            Aggregations aggregations = response.getAggregations();
            // 4.1.根据品牌名称，获取品牌结果
            List<String> brandList = getAggByName(aggregations, "brandAgg");
            result.put("品牌", brandList);
            // 4.2.根据品牌名称，获取品牌结果
            List<String> cityList = getAggByName(aggregations, "cityAgg");
            result.put("城市", cityList);
            // 4.3.根据品牌名称，获取品牌结果
            List<String> starList = getAggByName(aggregations, "starAgg");
            result.put("星级", starList);

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据聚合的名称获取聚合结果
     * @param aggregations
     * @param aggName
     * @return
     */
    private List<String> getAggByName(Aggregations aggregations, String aggName) {
        // 4.1.根据聚合名称获取聚合结果
        Terms brandTerms = aggregations.get(aggName);
        // 4.2.获取buckets
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        // 4.3.遍历
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            // 4.4.获取key
            String key = bucket.getKeyAsString();
            brandList.add(key);
        }
        return brandList;
    }

    /**
     * 处理搜索结果的聚合
     * @param request
     */
    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(100)
        );
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg")
                .field("city")
                .size(100)
        );
        request.source().aggregation(AggregationBuilders
                .terms("starAgg")
                .field("starName")
                .size(100)
        );
    }

    /**
     * 组合多个查询条件
     * - 关键词搜索放到must中，参与算分（品牌，星级，城市）
     * - 其他过滤条件放到filter中，不参与算分（价格）
     *
     * @param params 搜索关键字、城市、品牌、星级条件、价格条件，同时考虑广告的加权
     * @param request 搜索请求
     */
    private void buildBasicQuery(RequestParams params, SearchRequest request) {
        //     1. 构建布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //     2. 添加must、filter等条件
        String key = params.getKey();
        if (key == null || key.isEmpty()) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }

        //     （城市、品牌、星级条件）
        if (params.getCity() != null && !params.getCity().isEmpty()) {
            // 对应的DSL语句：
            // GET ... /hotel/_search {
            //   "query": {
            //     "bool": {
            //       "filter": {
            //         "term": {"city": "上海"}
            //       } #filter 不参与打分
            //     } #bool
            //   } #query
            // } #_search
            boolQuery.filter(QueryBuilders.termQuery("city", params.getCity()));
        }
        if (params.getBrand() != null && !params.getBrand().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("brand", params.getBrand()));
        }
        if (params.getStarName() != null && !params.getStarName().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("starName", params.getStarName()));
        }

        //     （价格条件）
        if (params.getMinPrice() != null && params.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price")
                    .gte(params.getMinPrice())
                    .lte(params.getMaxPrice()));
        }

        // 3. 算分控制（广告加权）
        // 之前用的是boolean查询，现在要改成function_score查询了
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                boolQuery, // 可以将之前写的boolean查询作为原始查询条件放到query中
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{ // 接下来是算分函数
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder( // 算分函数1
                                QueryBuilders.termQuery("isAD", true), // 算分函数1-term
                                ScoreFunctionBuilders.weightFactorFunction(10) // 算分函数1-加权模式
                        )
                });

        //     5. 放入source中
        request.source().query(functionScoreQueryBuilder);
    }

    /**
     * 抽取出响应的每一条文档进行反序列化成对象，返回对象列表
     *
     * @param response
     * @return 对象列表和总结果数封装成的PageResult
     */
    private PageResult handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        long totalHits = searchHits.getTotalHits().value;
        SearchHit[] hits = searchHits.getHits();
        List<HotelDoc> hotels = new ArrayList<>();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            //     反序列化
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            Object[] sortValues = hit.getSortValues();
            if (sortValues.length > 0) {
                Object sortValue = sortValues[0];
                hotelDoc.setDistance(sortValue);
            }
            hotels.add(hotelDoc);
        }
        return new PageResult(totalHits, hotels);
    }


}
