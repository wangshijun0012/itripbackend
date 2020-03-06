package cn.itrip.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripHotel;
import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.beans.pojo.ItripHotelRoom;
import cn.itrip.beans.pojo.ItripTradeEnds;
import cn.itrip.beans.vo.order.*;
import cn.itrip.beans.vo.store.StoreVO;
import cn.itrip.common.*;
import cn.itrip.service.hotel.ItripHotelService;
import cn.itrip.service.hotelorder.ItripHotelOrderService;
import cn.itrip.service.hotelroom.ItripHotelRoomService;
import cn.itrip.service.hoteltempstore.ItripHotelTempStoreService;
import cn.itrip.service.orderlinkuser.ItripOrderLinkUserService;
import cn.itrip.service.tradeends.ItripTradeEndsService;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by donghai on 2017/5/15.
 * <p/>
 * <p/>
 * 注：错误码（100501 ——100600）
 */
@Controller
@Api(value = "API", basePath = "/http://api.itrap.com/api")
@RequestMapping(value = "/api/hotelorder")
public class HotelOrderController {

    private Logger logger = Logger.getLogger(HotelOrderController.class);

    @Resource
    private ValidationToken validationToken;

    @Resource
    private ItripHotelService hotelService;

    @Resource
    private ItripHotelRoomService roomService;

    @Resource
    private ItripHotelTempStoreService tempStoreService;

    @Resource
    private SystemConfig systemConfig;

    @Resource
    private ItripHotelTempStoreService itripHotelTempStoreService;

    @Resource
    private ItripHotelOrderService itripHotelOrderService;

    @Resource
    private ItripTradeEndsService itripTradeEndsService;

    @Resource
    private ItripOrderLinkUserService itripOrderLinkUserService;

    @ApiOperation(value = "修改订房日期验证是否有房",httpMethod = "POST", response = Dto.class)
    @RequestMapping(value = "/validateroomstore",method = RequestMethod.POST)
    @ResponseBody
    public Dto validateRoomStore(@RequestBody ValidateRoomStoreVO vo, HttpServletRequest request){
        if(EmptyUtils.isEmpty(request.getHeader("token")) || !validationToken.getRedisAPI().hasKey(request.getHeader("token"))){
            return DtoUtil.returnFail("token失效，请重新登录","100000");
        }
        if(EmptyUtils.isEmpty(vo.getHotelId())){
            return DtoUtil.returnFail("hotelId不能为空","100515");
        }
        if(EmptyUtils.isEmpty(vo.getRoomId())){
            return DtoUtil.returnFail("roomId不能为空","100516");
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("hotelId",vo.getHotelId());
        map.put("roomId",vo.getRoomId());
        map.put("checkInDate",EmptyUtils.isEmpty(vo.getCheckInDate()) ? null : vo.getCheckInDate());
        map.put("checkOutDate",EmptyUtils.isEmpty(vo.getCheckOutDate()) ? null : vo.getCheckOutDate());
        map.put("count",EmptyUtils.isEmpty(vo.getCount()) ? 1 : vo.getCount());
        List<ItripHotelOrder> list = null;
        boolean flag = false;
        try {
            if (tempStoreService.validateRoomStore(map)){
                flag = true;
            }
            map.put("flag",flag);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常","100517");
        }
        return DtoUtil.returnDataSuccess(map);
    }
    @ApiOperation(value = "根据订单ID查看个人订单详情",httpMethod = "GET",response = Dto.class)
    @RequestMapping(value = "/getpersonalorderinfo/{orderId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto getPrisonalOrderInfo(@PathVariable String orderId,HttpServletRequest request){
        if(EmptyUtils.isEmpty(orderId)){
            return DtoUtil.returnFail("orderId不能为空","100525");
        }
        if(EmptyUtils.isEmpty(request.getHeader("token")) || !validationToken.getRedisAPI().hasKey(request.getHeader("token"))){
            return DtoUtil.returnFail("token失效，请重新登录","100000");
        }
            ItripHotelOrder order = null;
        try {
            order = itripHotelOrderService.getItripHotelOrderById(Long.parseLong(orderId));
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取个人订单信息错误","100527");
        }
        if(EmptyUtils.isEmpty(order)){
            return DtoUtil.returnFail("没有相关订单信息","100526");
        }
        ItripPersonalHotelOrderVO orderVO = new ItripPersonalHotelOrderVO();
        BeanUtils.copyProperties(order,orderVO);
        //查询房间预定信息
        if(!EmptyUtils.isEmpty(order.getRoomId())){
            try {
                ItripHotelRoom room = roomService.getItripHotelRoomById(order.getRoomId());
                order.setPayType(room.getPayType());
            } catch (Exception e) {
                e.printStackTrace();
                return DtoUtil.returnFail("获取个人订单信息错误","100527");
            }
        }
        //订单状态（0：待支付 1:已取消 2:支付成功 3:已消费 4:已点评）
        //{"1":"订单提交","2":"订单支付","3":"支付成功","4":"入住","5":"订单点评","6":"订单完成"}
        //{"1":"订单提交","2":"订单支付","3":"订单取消"}
        Integer orderStatus = order.getOrderStatus();
        if (orderStatus == 1) {
            orderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessCancel()));
            orderVO.setProcessNode("3");
        } else if (orderStatus == 0) {
            orderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
            orderVO.setProcessNode("2");//订单支付
        } else if (orderStatus == 2) {
            orderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
            orderVO.setProcessNode("3");//支付成功（未出行）
        } else if (orderStatus == 3) {
            orderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
            orderVO.setProcessNode("5");//订单点评
        } else if (orderStatus == 4) {
            orderVO.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
            orderVO.setProcessNode("6");//订单完成
        } else {
            orderVO.setOrderProcess(null);
            orderVO.setProcessNode(null);
        }
        BeanUtils.copyProperties(order,orderVO);
        return DtoUtil.returnDataSuccess(orderVO);
    }
    @ApiOperation(value = "根据订单ID查看个人订单详情-房型相关信息",httpMethod = "GET",response = Dto.class)
    @RequestMapping(value = "/getpersonalorderroominfo/{orderId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto getPersonalOrderRoomInfo(@PathVariable String orderId,HttpServletRequest request){
        if(EmptyUtils.isEmpty(request.getHeader("token")) || !validationToken.getRedisAPI().hasKey(request.getHeader("token"))){
            return DtoUtil.returnFail("token失效，请重新登录","100000");
        }
        if(EmptyUtils.isEmpty(orderId)){
            return DtoUtil.returnFail("orderId不能为空","100529");
        }
        ItripPersonalOrderRoomVO roomVO = null;
        try {
            roomVO = itripHotelOrderService.getItripHotelOrderRoomInfoById(Long.parseLong(orderId));
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取个人订单房型信息错误","100531");
        }
        return DtoUtil.returnDataSuccess(roomVO);
    }
    @ApiOperation(value = "根据个人订单列表，并分页显示",httpMethod = "POST",response = Dto.class)
    @RequestMapping(value = "/getpersonalorderlist",method = RequestMethod.POST)
    @ResponseBody
    public Dto getPresonalOrderList(@RequestBody ItripSearchOrderVO vo,HttpServletRequest request){
        if(EmptyUtils.isEmpty(request.getHeader("token")) || !validationToken.getRedisAPI().hasKey(request.getHeader("token"))){
            return DtoUtil.returnFail("token失效，请重新登录","100000");
        }
        if(EmptyUtils.isEmpty(vo.getOrderType()) ){
            return DtoUtil.returnFail("orderType不能为空","100501");
        }
        if(EmptyUtils.isEmpty(vo.getOrderStatus())){
            return DtoUtil.returnFail("orserStatus不能为空","100502");
        }
        Integer orderType = vo.getOrderType();
        Integer orderStatus = vo.getOrderStatus();
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderType", orderType == -1 ? null : orderType);
        map.put("orderStatus", orderStatus == -1 ? null : orderStatus);
        map.put("userId", validationToken.getCurrentUser(request.getHeader("token")).getId());
        map.put("orderNo", vo.getOrderNo());
        map.put("linkUserName", vo.getLinkUserName());
        map.put("startDate", vo.getStartDate());
        map.put("endDate", vo.getEndDate());
        Page<ItripListHotelOrderVO> voPage = null;
        try {
            voPage = itripHotelOrderService.queryOrderPageByMap(map, vo.getPageNo(), vo.getPageSize());
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取个人订单列表错误","100503 ");
        }
        return DtoUtil.returnDataSuccess(voPage);
    }
    @ApiOperation(value = "扫描中间表,执行库存更新操作",httpMethod = "GET",response = Dto.class)
    @RequestMapping(value = "/scanTradeEnd",method = RequestMethod.GET)
    @ResponseBody
    public Dto scanTradeEnd(){
        Map param = new HashMap();
        List<ItripTradeEnds> tradeEndses = null;
        try {
            param.put("flag", 1);
            param.put("oldFlag", 0);
            itripTradeEndsService.itriptxModifyItripTradeEnds(param);
            tradeEndses = itripTradeEndsService.getItripTradeEndsListByMap(param);
            if (EmptyUtils.isNotEmpty(tradeEndses)) {
                for (ItripTradeEnds ends : tradeEndses) {
                    Map<String, Object> orderParam = new HashMap<String, Object>();
                    orderParam.put("orderNo", ends.getOrderNo());
                    List<ItripHotelOrder> orderList = itripHotelOrderService.getItripHotelOrderListByMap(orderParam);
                    for (ItripHotelOrder order : orderList) {
                        Map<String, Object> roomStoreMap = new HashMap<String, Object>();
                        roomStoreMap.put("startTime", order.getCheckInDate());
                        roomStoreMap.put("endTime", order.getCheckOutDate());
                        roomStoreMap.put("count", order.getCount());
                        roomStoreMap.put("roomId", order.getRoomId());
                        tempStoreService.updateRoomStore(roomStoreMap);
                    }
                }
                param.put("flag", 2);
                param.put("oldFlag", 1);
                itripTradeEndsService.itriptxModifyItripTradeEnds(param);
                return DtoUtil.returnSuccess();
            }else{
                return DtoUtil.returnFail("100535", "没有查询到相应记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100536");
        }
    }
    @ApiOperation(value = "生成订单前,获取预订信息",httpMethod = "POST",response = Dto.class)
    @RequestMapping(value = "/getpreorderinfo",method = RequestMethod.POST)
    @ResponseBody
    public Dto getPreOrderInfo(@RequestBody ValidateRoomStoreVO vo,HttpServletRequest request){
        if(EmptyUtils.isEmpty(request.getHeader("token")) || !validationToken.getRedisAPI().hasKey(request.getHeader("token"))){
            return DtoUtil.returnFail("token失效，请重新登录","100000");
        }
        if(EmptyUtils.isEmpty(vo.getHotelId()) ){
            return DtoUtil.returnFail("hotelId不能为空","100510");
        }
        if(EmptyUtils.isEmpty(vo.getRoomId())){
            return DtoUtil.returnFail("roomId不能为空","100511");
        }
        ItripHotel hotel = null;
        ItripHotelRoom room = null;
        RoomStoreVO roomStoreVO = null;
        List<StoreVO> roomStoreVOList = null;
        roomStoreVO = new RoomStoreVO();
        Map map = new HashMap();
        map.put("startTime", vo.getCheckInDate());
        map.put("endTime", vo.getCheckOutDate());
        map.put("roomId", vo.getRoomId());
        map.put("hotelId", vo.getHotelId());
        try {
            hotel = hotelService.getItripHotelById(vo.getHotelId());
            room = roomService.getItripHotelRoomById(vo.getRoomId());
            roomStoreVOList = tempStoreService.queryRoomStore(map);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常","100513");
        }
        BeanUtils.copyProperties(vo,roomStoreVO);
        BeanUtils.copyProperties(hotel,roomStoreVO);
        BeanUtils.copyProperties(room,roomStoreVO);
        roomStoreVO.setCount(1);
        if(EmptyUtils.isEmpty(roomStoreVO)){
            return DtoUtil.returnFail("暂时无房", "100512");
        }
        roomStoreVO.setStore(roomStoreVOList.get(0).getStore());
        return DtoUtil.returnDataSuccess(roomStoreVO);


    }




}
