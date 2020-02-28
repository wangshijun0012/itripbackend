package cn.itrip.service.searchfacilities;

import cn.itrip.beans.vo.hotel.ItripSearchFacilitiesHotelVO;

public interface HotelFacilitiesService {
    ItripSearchFacilitiesHotelVO gethotelfacilities(Integer id) throws Exception;
}
