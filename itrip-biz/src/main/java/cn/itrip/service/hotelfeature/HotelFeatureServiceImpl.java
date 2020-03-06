package cn.itrip.service.hotelfeature;

import cn.itrip.beans.pojo.ItripHotelFeature;
import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.hotel.ItripSearchDetailsHotelVO;
import cn.itrip.common.Constants;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.Page;
import cn.itrip.dao.hotel.ItripHotelMapper;
import cn.itrip.dao.hotelfeature.ItripHotelFeatureMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HotelFeatureServiceImpl implements HotelFeatureService {
    @Resource
    private ItripHotelFeatureMapper itripHotelFeatureMapper;
    @Resource
    private ItripHotelMapper itripHotelMapper;
    @Override
    public Page<ItripHotelFeature> queryItripHotelFeaturePageByMap(Map<String,Object> param, Integer pageNo, Integer pageSize)throws Exception{
        Integer total = itripHotelFeatureMapper.getItripHotelFeatureCountByMap(param);
        pageNo = EmptyUtils.isEmpty(pageNo) ? Constants.DEFAULT_PAGE_NO : pageNo;
        pageSize = EmptyUtils.isEmpty(pageSize) ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        Page page = new Page(pageNo, pageSize, total);
        param.put("beginPos", page.getBeginPos());
        param.put("pageSize", page.getPageSize());
        List<ItripHotelFeature> itripHotelFeatureList = itripHotelFeatureMapper.getItripHotelFeatureListByMap(param);
        page.setRows(itripHotelFeatureList);
        return page;
    }

    @Override
    public List<ItripSearchDetailsHotelVO> getHotelDetails(Integer id) throws Exception {
        List<ItripLabelDic> list = itripHotelMapper.getHotelFeatureByHotelId(Long.valueOf(id));
        ArrayList arrayList = new ArrayList<>();
        for (ItripLabelDic dic : list){
            ItripSearchDetailsHotelVO vo = new ItripSearchDetailsHotelVO();
            BeanUtils.copyProperties(dic,vo);
            arrayList.add(vo);
        }
        return arrayList;
    }
}
