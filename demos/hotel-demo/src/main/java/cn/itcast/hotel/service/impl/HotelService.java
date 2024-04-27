package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            String key = params.getKey();
            if (key == null || key.isEmpty()) {
                request.source().query(QueryBuilders.matchAllQuery());
            } else {
                request.source().query(QueryBuilders.matchQuery("all", key));
            }
            //     2.2 分页
            Integer page = params.getPage();
            Integer size = params.getSize();
            request.source().from((page - 1) * size).size(size);

            //     3. 发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //     4. 解析响应
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 抽取出响应的每一条文档进行反序列化成对象，返回对象列表
     * @param response
     * @return 对象列表和总结果数封装成的PageResult
     */
    private PageResult handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        long totalHits = searchHits.getTotalHits().value;
        System.out.println("结果总数" + totalHits);
        SearchHit[] hits = searchHits.getHits();
        List<HotelDoc> hotels = new ArrayList<>();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
        //     反序列化
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            hotels.add(hotelDoc);
        }
        return new PageResult(totalHits, hotels);
    }


}
