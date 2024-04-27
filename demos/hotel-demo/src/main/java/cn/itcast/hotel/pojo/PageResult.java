package cn.itcast.hotel.pojo;
import lombok.Data;

import java.util.List;
/**
 * 搜索查询 返回结果
 *
 * @author Li Yijia
 * @date 2024/4/27
 */
@Data
public class PageResult {
    /**
     * 总条数
     */
    private Long total;

    /**
     * 当前页的数据
     */
    private List<HotelDoc> hotels;


    public PageResult(Long total, List<HotelDoc> hotels) {
        this.total = total;
        this.hotels = hotels;
    }
}
