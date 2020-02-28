package cn.itrip.service.videodesc;

import cn.itrip.beans.vo.hotel.HotelVideoDescVO;

public interface VideoDescService {
    HotelVideoDescVO getVideoDesc(String hotelId) throws Exception;

}
