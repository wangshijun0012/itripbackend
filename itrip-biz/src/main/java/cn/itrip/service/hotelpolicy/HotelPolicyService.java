package cn.itrip.service.hotelpolicy;

import cn.itrip.beans.vo.hotel.ItripSearchPolicyHotelVO;

public interface HotelPolicyService {
    ItripSearchPolicyHotelVO getHotelPolicy(Integer id) throws Exception;
}
