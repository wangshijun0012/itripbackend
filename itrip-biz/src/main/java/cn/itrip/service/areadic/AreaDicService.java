package cn.itrip.service.areadic;

import cn.itrip.beans.vo.ItripAreaDicVO;

import java.util.List;

public interface AreaDicService {
    List<ItripAreaDicVO> getAreaVoDicList(Integer cityId) throws Exception;
    List<ItripAreaDicVO> getAreaVoDicListByType(Integer type) throws Exception;
}
