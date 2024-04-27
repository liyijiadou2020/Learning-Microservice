package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static cn.itcast.hotel.constants.HotelIndexConstants.MAPPING_TEMPLATE;
/**
 * 使用 RestHighLevelClient 查询索引库
 */
@SpringBootTest
class HotelIndexTest {

    private RestHighLevelClient client;

    /**
     * 创建索引库
     *
     * @throws IOException
     */
    @Test
    void testCreateIndex() throws IOException {
        // 1.准备Request      PUT /hotel
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // 2.准备请求参数
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        // 3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 查看索引库的存在情况
     * @throws IOException
     */
    @Test
    void testExistsIndex() throws IOException {
        // 1.准备Request
        GetIndexRequest request = new GetIndexRequest("hotel");
        // 3.发送请求
        boolean isExists = client.indices().exists(request, RequestOptions.DEFAULT);

        System.out.println(isExists ? "存在" : "不存在");
    }

    /**
     * 删除已存在的索引库
     * @throws IOException
     */
    @Test
    void testDeleteIndex() throws IOException {
        // 1.准备Request
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        // 3.发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }


    /**
     * 初始化客户端
     */
    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                // 改成自己的地址
                HttpHost.create("http://192.168.10.120:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }


}
