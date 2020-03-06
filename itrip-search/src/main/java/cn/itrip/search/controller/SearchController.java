package cn.itrip.search.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.hotel.SearchHotCityVO;
import cn.itrip.beans.vo.hotel.SearchHotelVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.ErrorCode;
import cn.itrip.common.Page;
import cn.itrip.search.bean.ItripHotelVO;
import cn.itrip.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
@Api(value = "酒店查询接口",tags = "酒店查询接口")
@Controller
@RequestMapping("/api/hotellist")
public class SearchController {
    @Resource
    private SearchService searchService;
    @ApiOperation(value = "查询热门酒店接口",response = Dto.class,httpMethod = "POST")
    @RequestMapping(value="/searchItripHotelListByHotCity",method = RequestMethod.POST)
    @ResponseBody
    public Dto<ItripHotelVO> queryHotCity(@RequestBody SearchHotCityVO vo){
        if(vo.getCityId() == null){
            return DtoUtil.returnFail("城市id不能为空", ErrorCode.SEARCH_CITYID_NOTFOUND);
        }else{
            List<ItripHotelVO> list = null;
            try {
                list = searchService.queryHotCity(vo.getCityId(),vo.getCount());
            } catch (Exception e) {
                e.printStackTrace();
                return DtoUtil.returnFail(e.getMessage(),ErrorCode.SEARCH_UNKNOWN);
            }
            return DtoUtil.returnDataSuccess(list);
        }
    }
    @ApiOperation(value = "查询热门酒店分页接口",response = Dto.class,httpMethod = "POST")
    @RequestMapping(value="/searchItripHotelPage",method = RequestMethod.POST)
    @ResponseBody
    public Dto<ItripHotelVO> queryHotel(@RequestBody SearchHotelVO vo){
        if(EmptyUtils.isEmpty(vo) || EmptyUtils.isEmpty(vo.getDestination())){
            return DtoUtil.returnFail("目的地不能为空",ErrorCode.SEARCH_DESTINATION_NOTNULL);
        }
        Page<ItripHotelVO> page = null;
        try {
            page = searchService.queryHotelPage(vo,vo.getPageNo(),vo.getPageSize());
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.SEARCH_UNKNOWN);
        }
        return DtoUtil.returnDataSuccess(page);
    }
}
