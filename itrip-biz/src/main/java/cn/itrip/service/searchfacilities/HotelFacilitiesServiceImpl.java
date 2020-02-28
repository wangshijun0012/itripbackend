package cn.itrip.service.searchfacilities;

import cn.itrip.beans.vo.hotel.ItripSearchFacilitiesHotelVO;
import cn.itrip.dao.hotel.ItripHotelMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class HotelFacilitiesServiceImpl implements HotelFacilitiesService {
    @Resource
    private ItripHotelMapper itripHotelMapper;
    @Override
    public ItripSearchFacilitiesHotelVO gethotelfacilities(Integer id) throws Exception {
        ItripSearchFacilitiesHotelVO hotelFacilitiesById = itripHotelMapper.getItripHotelFacilitiesById(Long.valueOf(id));
        return hotelFacilitiesById;
    }
}
