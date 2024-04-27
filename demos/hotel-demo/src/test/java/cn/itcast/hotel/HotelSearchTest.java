package cn.itcast.hotel;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
/**
 * 使用RestClient 查询文档
 *
 * @author Li Yijia
 * @date 2024/4/26
 */

@SpringBootTest
public class HotelSearchTest {

    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.10.120:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }

    /**
     * 发起查询请求
     * @throws IOException
     */
    @Test
    void testMatchAll() throws IOException {
        // 准备request对象
        SearchRequest request = new SearchRequest("hotel");
        // 准备DSL
        request.source().query(QueryBuilders.matchAllQuery());
        // 发送对象
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    /**
     * 发起查询请求并解析响应结果
     * @throws IOException
     */
    @Test
    void testMatchAllAndParse() throws IOException {
        // 准备request对象
        SearchRequest request = new SearchRequest("hotel");
        // 准备DSL
        request.source().query(QueryBuilders.matchAllQuery());
        // 发送对象
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //     解析结果
        SearchHits searchHits = response.getHits();
        TotalHits totalHits = searchHits.getTotalHits();
        System.out.println("结果总数" + totalHits);
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit: hits) {
            String json = hit.getSourceAsString();
            System.out.println(json);
        }
    }




}
