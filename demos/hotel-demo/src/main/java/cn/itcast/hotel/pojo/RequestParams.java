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

    /**
     * 城市筛选条件
     */
    private String city;

    /**
     * 品牌筛选条件
     */
    private String brand;

    /**
     * 星级筛选条件
     */
    private String starName;

    /**
     * 最低价格筛选条件
     */
    private Integer minPrice;

    /**
     * 最高价格筛选条件
     */
    private Integer maxPrice;

    /**
     * 我的地理位置
     */
    private String location;

    /**
     * 酒店图片uri
     */
    private String pic;

    /**
     * 距离值
     */
    private Object distance;

}
