package cn.itrip.service.videodesc;

import cn.itrip.beans.pojo.ItripAreaDic;
import cn.itrip.beans.pojo.ItripHotel;
import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.hotel.HotelVideoDescVO;
import cn.itrip.dao.hotel.ItripHotelMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoDescServiceImpl implements VideoDescService {
    @Resource
    private ItripHotelMapper itripHotelMapper;

    @Override
    public HotelVideoDescVO getVideoDesc(String hotelId) throws Exception {
        ItripHotel hotel = itripHotelMapper.getItripHotelById(Long.parseLong(hotelId));
        String hotelName = hotel.getHotelName();
        List<ItripAreaDic> itripAreaDicList = itripHotelMapper.getHotelAreaByHotelId(Long.parseLong(hotelId));
        ArrayList<String> tradingAreaNameList = new ArrayList<>();
        for (ItripAreaDic areaDic : itripAreaDicList) {
            tradingAreaNameList.add(areaDic.getName());
        }
        ArrayList<String> hotelFeatureList = new ArrayList<>();
        List<ItripLabelDic> hotelFeatureByHotelId = itripHotelMapper.getHotelFeatureByHotelId(Long.parseLong(hotelId));
        for( ItripLabelDic itripLabelDic : hotelFeatureByHotelId){
            String description = itripLabelDic.getDescription();
            hotelFeatureList.add(description);
        }
        HotelVideoDescVO vo = new HotelVideoDescVO();
        vo.setHotelName(hotelName);
        vo.setTradingAreaNameList(tradingAreaNameList);
        vo.setHotelFeatureList(hotelFeatureList);
        return vo;
    }

}
