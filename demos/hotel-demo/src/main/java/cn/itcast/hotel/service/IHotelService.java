package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IHotelService extends IService<Hotel> {
    /**
     * 根据关键字搜索酒店数据
     * @param params
     * @return
     */
    PageResult search(RequestParams params);
}
