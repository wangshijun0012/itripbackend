package cn.itrip.service.hotelimg;

import cn.itrip.beans.vo.ItripImageVO;

import java.util.List;

public interface HotelImgService {
    List<ItripImageVO> getImgById(String id) throws Exception;
}
