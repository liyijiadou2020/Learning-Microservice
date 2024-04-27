package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {
    /**
     * 根据关键字搜索酒店数据
     * @param params
     * @return
     */
    PageResult search(RequestParams params);

    /**
     * 对搜索结果做聚合，限定显示的城市、星级、品牌列表
     * @param params
     * @return
     */
    Map<String, List<String>> filters(RequestParams params);
}
