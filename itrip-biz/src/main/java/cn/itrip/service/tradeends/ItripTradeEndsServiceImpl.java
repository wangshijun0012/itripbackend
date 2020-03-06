package cn.itrip.service.tradeends;
import cn.itrip.beans.pojo.ItripTradeEnds;
import cn.itrip.common.Constants;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.Page;
import cn.itrip.dao.tradeends.ItripTradeEndsMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
@Service
public class ItripTradeEndsServiceImpl implements ItripTradeEndsService {

    @Resource
    private ItripTradeEndsMapper itripTradeEndsMapper;

    @Override
    public ItripTradeEnds getItripTradeEndsById(Long id)throws Exception{
        return itripTradeEndsMapper.getItripTradeEndsById(id);
    }

    @Override
    public List<ItripTradeEnds>	getItripTradeEndsListByMap(Map<String,Object> param)throws Exception{
        return itripTradeEndsMapper.getItripTradeEndsListByMap(param);
    }

    @Override
    public Integer getItripTradeEndsCountByMap(Map<String,Object> param)throws Exception{
        return itripTradeEndsMapper.getItripTradeEndsCountByMap(param);
    }

    @Override
    public Integer itriptxAddItripTradeEnds(ItripTradeEnds itripTradeEnds)throws Exception{
            return itripTradeEndsMapper.insertItripTradeEnds(itripTradeEnds);
    }

    @Override
    public Integer itriptxModifyItripTradeEnds(Map<String,Object> param)throws Exception{
        return itripTradeEndsMapper.updateItripTradeEnds(param);
    }

    @Override
    public Integer itriptxDeleteItripTradeEndsById(Long id)throws Exception{
        return itripTradeEndsMapper.deleteItripTradeEndsById(id);
    }

    @Override
    public Page<ItripTradeEnds> queryItripTradeEndsPageByMap(Map<String,Object> param, Integer pageNo, Integer pageSize)throws Exception{
        Integer total = itripTradeEndsMapper.getItripTradeEndsCountByMap(param);
        pageNo = EmptyUtils.isEmpty(pageNo) ? Constants.DEFAULT_PAGE_NO : pageNo;
        pageSize = EmptyUtils.isEmpty(pageSize) ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        Page page = new Page(pageNo, pageSize, total);
        param.put("beginPos", page.getBeginPos());
        param.put("pageSize", page.getPageSize());
        List<ItripTradeEnds> itripTradeEndsList = itripTradeEndsMapper.getItripTradeEndsListByMap(param);
        page.setRows(itripTradeEndsList);
        return page;
    }

}
