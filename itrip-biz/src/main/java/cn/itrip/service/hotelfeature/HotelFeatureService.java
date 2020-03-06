package cn.itrip.service.hotelfeature;

import cn.itrip.beans.pojo.ItripHotelFeature;
import cn.itrip.beans.vo.hotel.ItripSearchDetailsHotelVO;
import cn.itrip.common.Page;

import java.util.List;
import java.util.Map;

public interface HotelFeatureService {
    Page<ItripHotelFeature> queryItripHotelFeaturePageByMap(Map<String,Object> param, Integer pageNo, Integer pageSize)throws Exception;
    List<ItripSearchDetailsHotelVO> getHotelDetails(Integer id) throws Exception;
}
