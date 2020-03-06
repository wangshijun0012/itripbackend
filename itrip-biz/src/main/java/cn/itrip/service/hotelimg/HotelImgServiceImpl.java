package cn.itrip.service.hotelimg;

import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.dao.image.ItripImageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
@Service
public class HotelImgServiceImpl implements HotelImgService {
    @Resource
    private ItripImageMapper itripImageMapper;
    @Override
    public List<ItripImageVO> getImgById(String targetId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("targetId",targetId);
        //注意下面value为String类型的0，不然会自动视为null而忽略该字段的匹配。
        map.put("type","0");
        List<ItripImageVO> imageVOList = itripImageMapper.getItripImageListByMap(map);
        return imageVOList;
    }
}
