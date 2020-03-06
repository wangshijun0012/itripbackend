package cn.itrip.service.hotelpolicy;

import cn.itrip.beans.vo.hotel.ItripSearchPolicyHotelVO;
import cn.itrip.dao.hotel.ItripHotelMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class HotelPolicyServiceImpl implements HotelPolicyService {
    @Resource
    private ItripHotelMapper itripHotelMapper;
    @Override
    public ItripSearchPolicyHotelVO getHotelPolicy(Integer id) throws Exception {
        ItripSearchPolicyHotelVO vo = itripHotelMapper.queryHotelPolicy(Long.valueOf(id));
        return vo;
    }
}
