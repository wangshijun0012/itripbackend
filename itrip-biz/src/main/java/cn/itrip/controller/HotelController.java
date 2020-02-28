package cn.itrip.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.ItripAreaDicVO;
import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.beans.vo.ItripLabelDicVO;
import cn.itrip.beans.vo.hotel.HotelVideoDescVO;
import cn.itrip.beans.vo.hotel.ItripSearchDetailsHotelVO;
import cn.itrip.beans.vo.hotel.ItripSearchFacilitiesHotelVO;
import cn.itrip.beans.vo.hotel.ItripSearchPolicyHotelVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.ErrorCode;
import cn.itrip.dao.labeldic.ItripLabelDicMapper;
import cn.itrip.service.areadic.AreaDicService;
import cn.itrip.service.hotelfeature.HotelFeatureService;
import cn.itrip.service.hotelimg.HotelImgService;
import cn.itrip.service.hotelpolicy.HotelPolicyService;
import cn.itrip.service.searchfacilities.HotelFacilitiesService;
import cn.itrip.service.videodesc.VideoDescService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "酒店商圈查询接口",tags = "酒店商圈查询接口")
@Controller
@RequestMapping("/api/hotel")
public class HotelController {
    @Resource
    private AreaDicService areaDicService;
    @Resource
    private HotelImgService hotelImgService;
    @Resource
    private VideoDescService videoDescService;
    @Resource
    private HotelFacilitiesService hotelfacilitiesService;
    @Resource
    private HotelFeatureService hotelFeatureService;
    @Resource
    private HotelPolicyService hotelPolicyService;
    @Resource
    private ItripLabelDicMapper itripLabelDicService;
    @ApiOperation(value = "根据城市id查询商圈", response = Dto.class, httpMethod = "GET")
    @RequestMapping(value = "/querytradearea/{cityId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto queryTradeArea(@PathVariable(value = "cityId") Integer cityId) {
        if (EmptyUtils.isEmpty(cityId)) {
            return DtoUtil.returnFail("城市id不能为空", ErrorCode.AREA_CITYID_NOTNULL);
        }
        List<ItripAreaDicVO> voDicList = null;
        try {
            voDicList = areaDicService.getAreaVoDicList(cityId);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常，获取失败", ErrorCode.AEEA_SYSTEM_ERROR);
        }
        return DtoUtil.returnDataSuccess(voDicList);
    }

    @ApiOperation(value = "根据targetId查询酒店图片(type=0)", httpMethod = "GET", response = Dto.class)
    @RequestMapping(value = "/getimg/{targetId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto getimg(@PathVariable String targetId) {
        if (EmptyUtils.isEmpty(targetId)) {
            return DtoUtil.returnFail("酒店id不能为空", ErrorCode.IMG_HOTELID_NOTNULL);
        }
        List<ItripImageVO> voList = null;
        try {
            voList = hotelImgService.getImgById(targetId);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店图片失败", ErrorCode.IMG_SYSTEM_ERROR);
        }
        return DtoUtil.returnDataSuccess(voList);
    }

    @ApiOperation(value = "根据酒店id查询酒店特色、商圈、酒店名称", httpMethod = "GET", response = Dto.class)
    @RequestMapping(value = "/getvideodesc/{hotelId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto getVideoDesc(@PathVariable String hotelId) {
        if (EmptyUtils.isEmpty(hotelId)) {
            return DtoUtil.returnFail("酒店id不能为空", ErrorCode.VIDEO_HOTELID_NOTNULL);
        }
        HotelVideoDescVO vo = null;
        try {
            vo = videoDescService.getVideoDesc(hotelId);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店视频描述失败", ErrorCode.VIDEO_SYSTEM_ERROR);
        }
        return DtoUtil.returnDataSuccess(vo);
    }

    @ApiOperation(value = "根据酒店id查询酒店设施", httpMethod = "GET", response = Dto.class)
    @RequestMapping(value = "/queryhotelfacilities/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Dto getHotelfacilities(@PathVariable Integer id) {
        if (EmptyUtils.isEmpty(id)) {
            return DtoUtil.returnFail("酒店id不能为空", ErrorCode.FACILITY_CITYID_NOTNULL);
        }
        ItripSearchFacilitiesHotelVO vo = null;
        try {
            vo = hotelfacilitiesService.gethotelfacilities(id);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常，获取失败", ErrorCode.FACILITY_SYSTEM_ERROR);
        }
        return DtoUtil.returnDataSuccess(vo);
    }

    @ApiOperation(value = "获取酒店特色(用于查询页列表)", response = Dto.class, httpMethod = "GET")
    @RequestMapping(value = "/queryhotelfeature", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripLabelDicVO> queryHotelFeature() {
        List<ItripLabelDic> itripLabelDics = null;
        List<ItripLabelDicVO> itripAreaDicVOs = null;
        try {
            Map param = new HashMap();
            param.put("parentId", 16);
            itripLabelDics = itripLabelDicService.getItripLabelDicListByMap(param);
            if (EmptyUtils.isNotEmpty(itripLabelDics)) {
                itripAreaDicVOs = new ArrayList();
                for (ItripLabelDic dic : itripLabelDics) {
                    ItripLabelDicVO vo = new ItripLabelDicVO();
                    BeanUtils.copyProperties(dic, vo);
                    itripAreaDicVOs.add(vo);
                }
            }

        } catch (Exception e) {
            DtoUtil.returnFail("系统异常", "10205");
            e.printStackTrace();
        }
        return DtoUtil.returnDataSuccess(itripAreaDicVOs);
    }
    @ApiOperation(value = "查询国内、国外的热门城市(1:国内 2:国外)", response = Dto.class, httpMethod = "GET")
    @RequestMapping(value = "/queryhotcity/{type}", method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotCity(@PathVariable Integer type) {
        if (EmptyUtils.isEmpty(type)) {
            return DtoUtil.returnFail("type不能为空", ErrorCode.HOTCITY_TYPE_NOTNULL);
        }
        List<ItripAreaDicVO> voList = null;
        try {
            voList = areaDicService.getAreaVoDicListByType(type);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常，获取失败", ErrorCode.HOTCITY_SYSTEM_ERROR);
        }
        return DtoUtil.returnDataSuccess(voList);
    }
    @ApiOperation(value = "根据酒店id查询酒店特色和介绍", response = Dto.class, httpMethod = "GET")
    @RequestMapping(value = "/queryhoteldetails/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Dto queryhoteldetails(@PathVariable Integer id) {
        if(EmptyUtils.isEmpty(id)){
            return DtoUtil.returnFail("酒店id不能为空",ErrorCode.DETAILS_HOTELID_NOTNULL);
        }
        List<ItripSearchDetailsHotelVO> detailsHotelVOS = null;
        try {
            detailsHotelVOS = hotelFeatureService.getHotelDetails(id);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常，获取失败",ErrorCode.DETAILS_SYSTEM_ERROR);
        }
        return DtoUtil.returnDataSuccess(detailsHotelVOS);
    }
    @ApiOperation(value = "根据酒店id查询酒店政策", response = Dto.class, httpMethod = "GET")
    @RequestMapping(value = "/queryhotelpolicy/{id}",  method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotelPolicy(@PathVariable Integer id){
        if(EmptyUtils.isEmpty(id)){
            return DtoUtil.returnFail("酒店id不能为空",ErrorCode.POLICY_HOTELID_NOTNULL);
        }
        ItripSearchPolicyHotelVO vo = null;
        try {
            vo = hotelPolicyService.getHotelPolicy(id);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常，获取失败",ErrorCode.POLICY_SYSTEM_ERROR);
        }
        return DtoUtil.returnDataSuccess(vo);
    }
}
