package cn.itcast.hotel.pojo;
import lombok.Data;
/**
 * 搜索查询-前端的请求参数实体类
 *
 * @author Li Yijia
 * @date 2024/4/27
 */

@Data
public class RequestParams {
    /**
     * 关键字
     */
    private String key;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 排序，暂时不实现
     */
    private String sortBy;

}
