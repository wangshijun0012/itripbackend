package cn.itrip.service.areadic;

import cn.itrip.beans.pojo.ItripAreaDic;
import cn.itrip.beans.vo.ItripAreaDicVO;
import cn.itrip.dao.areadic.ItripAreaDicMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class AreaDicServiceImpl implements AreaDicService {
    @Resource
    private ItripAreaDicMapper itripAreaDicMapper;
    @Override
    public List<ItripAreaDicVO> getAreaVoDicList(Integer cityId) throws Exception {
        Map map = new HashMap();
        map.put("isTradingArea", 1);
        map.put("parent", cityId);
        List<ItripAreaDic> listByMap = itripAreaDicMapper.getItripAreaDicListByMap(map);
        ArrayList<ItripAreaDicVO> voArrayList = new ArrayList<>();
        for(ItripAreaDic dic : listByMap){
            ItripAreaDicVO vo = new ItripAreaDicVO();
            BeanUtils.copyProperties(dic,vo);
            voArrayList.add(vo);
        }
        return voArrayList;
    }

    @Override
    public List<ItripAreaDicVO> getAreaVoDicListByType(Integer type) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("isChina",type);
        map.put("isHot",1);
        List<ItripAreaDic> list = itripAreaDicMapper.getItripAreaDicListByMap(map);
        ArrayList<ItripAreaDicVO> arrayList = new ArrayList<>();
        for (ItripAreaDic dic : list){
            ItripAreaDicVO vo = new ItripAreaDicVO();
            BeanUtils.copyProperties(dic,vo);
            arrayList.add(vo);
        }
        return arrayList;
    }
}
