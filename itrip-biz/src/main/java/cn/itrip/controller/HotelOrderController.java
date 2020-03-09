package cn.itrip.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.*;
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
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Api(value = "订单信息接口",tags = "订单信息接口")
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
        boolean flag = false;
        try {
            flag = tempStoreService.validateRoomStore(map);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常","100517");
        }
        map.put("flag", flag);
        return DtoUtil.returnSuccess("操作成功", map);



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
        BeanUtils.copyProperties(order,orderVO);
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
        roomStoreVO.setCheckInDate(vo.getCheckInDate());
        roomStoreVO.setCheckOutDate(vo.getCheckOutDate());
        roomStoreVO.setHotelName(hotel.getHotelName());
        roomStoreVO.setRoomId(room.getId());
        roomStoreVO.setPrice(room.getRoomPrice());
        roomStoreVO.setHotelId(vo.getHotelId());
        roomStoreVO.setCount(EmptyUtils.isEmpty(vo.getCount()) ? 1 : vo.getCount() );
        if(EmptyUtils.isEmpty(roomStoreVOList)){
            return DtoUtil.returnFail("暂时无房", "100512");
        }
        roomStoreVO.setStore(roomStoreVOList.get(0).getStore());
        return DtoUtil.returnDataSuccess(roomStoreVO);
    }
    @ApiOperation(value = "支付成功后查询订单信息",httpMethod = "POST",response = Dto.class)
    @RequestMapping(value = "/querysuccessorderinfo/{id}",method = RequestMethod.POST)
    @ResponseBody
    public Dto querySuccessOrderInfo(@PathVariable Integer id,HttpServletRequest request){
        if(EmptyUtils.isEmpty(request.getHeader("token")) || !validationToken.getRedisAPI().hasKey(request.getHeader("token"))){
            return DtoUtil.returnFail("token失效，请重新登录","100000");
        }
        if(EmptyUtils.isEmpty(id) ){
            return DtoUtil.returnFail("hotelId不能为空","100510");
        }
        try {
            ItripHotelOrder order = itripHotelOrderService.getItripHotelOrderById(Long.valueOf(id));
            if (EmptyUtils.isEmpty(order)) {
                return DtoUtil.returnFail("没有查询到相应订单", "100519");
            }
            ItripHotelRoom room = roomService.getItripHotelRoomById(order.getRoomId());
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", order.getId());
            resultMap.put("orderNo", order.getOrderNo());
            resultMap.put("payType", order.getPayType());
            resultMap.put("payAmount", order.getPayAmount());
            resultMap.put("hotelName", order.getHotelName());
            resultMap.put("roomTitle", room.getRoomTitle());
            return DtoUtil.returnSuccess("获取数据成功", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取数据失败", "100520");
        }
    }
    @ApiOperation(value = "生成订单", httpMethod = "POST",response = Dto.class)
    @RequestMapping(value = "/addhotelorder", method = RequestMethod.POST)
    @ResponseBody
    public Dto<Object> addHotelOrder(@RequestBody ItripAddHotelOrderVO itripAddHotelOrderVO, HttpServletRequest request) {
        Dto<Object> dto = new Dto<Object>();
        String tokenString = request.getHeader("token");
        logger.debug("token name is from header : " + tokenString);
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        Map<String, Object> validateStoreMap = new HashMap<String, Object>();
        validateStoreMap.put("startTime", itripAddHotelOrderVO.getCheckInDate());
        validateStoreMap.put("endTime", itripAddHotelOrderVO.getCheckOutDate());
        validateStoreMap.put("hotelId", itripAddHotelOrderVO.getHotelId());
        validateStoreMap.put("roomId", itripAddHotelOrderVO.getRoomId());
        validateStoreMap.put("count", itripAddHotelOrderVO.getCount());
        List<ItripUserLinkUser> linkUserList = itripAddHotelOrderVO.getLinkUser();
        if(EmptyUtils.isEmpty(currentUser)){
            return DtoUtil.returnFail("token失效，请重登录", "100000");
        }
        try {
            //判断库存是否充足
            Boolean flag = itripHotelTempStoreService.validateRoomStore(validateStoreMap);
            if (flag && null != itripAddHotelOrderVO) {
                //计算订单的预定天数
                Integer days = DateUtil.getBetweenDates(
                        itripAddHotelOrderVO.getCheckInDate(), itripAddHotelOrderVO.getCheckOutDate()
                ).size()-1;
                if(days<=0){
                    return DtoUtil.returnFail("退房日期必须大于入住日期", "100505");
                }

                ItripHotelOrder itripHotelOrder = new ItripHotelOrder();
                BeanUtils.copyProperties(itripAddHotelOrderVO,itripHotelOrder);
                itripHotelOrder.setId(itripAddHotelOrderVO.getId());
                itripHotelOrder.setUserId(currentUser.getId());
                itripHotelOrder.setCreatedBy(currentUser.getId());
                StringBuilder linkUserName = new StringBuilder();
                int size = linkUserList.size();
                for (int i = 0; i < size; i++) {
                    if (i != size - 1) {
                        linkUserName.append(linkUserList.get(i).getLinkUserName() + ",");
                    } else {
                        linkUserName.append(linkUserList.get(i).getLinkUserName());
                    }
                }
                itripHotelOrder.setLinkUserName(linkUserName.toString());
                itripHotelOrder.setBookingDays(days);
                if (tokenString.startsWith("token:PC")) {
                    itripHotelOrder.setBookType(0);
                } else if (tokenString.startsWith("token:MOBILE")) {
                    itripHotelOrder.setBookType(1);
                } else {
                    itripHotelOrder.setBookType(2);
                }
                //支付之前生成的订单的初始状态为未支付
                itripHotelOrder.setOrderStatus(0);
                try {
                    //生成订单号：机器码 +日期+（MD5）（商品IDs+毫秒数+1000000的随机数）
                    StringBuilder md5String = new StringBuilder();
                    md5String.append(itripHotelOrder.getHotelId());
                    md5String.append(itripHotelOrder.getRoomId());
                    md5String.append(System.currentTimeMillis());
                    md5String.append(Math.random() * 1000000);
                    String md5 = MD5.getMd5(md5String.toString(), 6);

                    //生成订单编号
                    StringBuilder orderNo = new StringBuilder();
                    orderNo.append(systemConfig.getMachineCode());
                    orderNo.append(DateUtil.format(new Date(), "yyyyMMddHHmmss"));
                    orderNo.append(md5);
                    itripHotelOrder.setOrderNo(orderNo.toString());
                    //计算订单的总金额
                    itripHotelOrder.setPayAmount(itripHotelOrderService.getOrderPayAmount(days * itripAddHotelOrderVO.getCount(), itripAddHotelOrderVO.getRoomId()));

                    Map<String, String> map = itripHotelOrderService.itriptxAddItripHotelOrder(itripHotelOrder, linkUserList);
                    DtoUtil.returnSuccess();
                    dto = DtoUtil.returnSuccess("生成订单成功", map);
                } catch (Exception e) {
                    e.printStackTrace();
                    dto = DtoUtil.returnFail("生成订单失败", "100505");
                }
            } else if (flag && null == itripAddHotelOrderVO) {
                dto = DtoUtil.returnFail("不能提交空，请填写订单信息", "100506");
            } else {
                dto = DtoUtil.returnFail("库存不足", "100507");
            }
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100508");
        }
    }
    @ApiOperation(value = "根据订单ID获取订单信息", httpMethod = "GET", response = Dto.class)
    @RequestMapping(value = "/queryOrderById/{orderId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto<Object> queryOrderById(@ApiParam(required = true, name = "orderId", value = "订单ID") @PathVariable Long orderId, HttpServletRequest request) {
        ItripModifyHotelOrderVO itripModifyHotelOrderVO = null;
        try {
            String tokenString = request.getHeader("token");
            ItripUser currentUser = validationToken.getCurrentUser(tokenString);
            if(EmptyUtils.isEmpty(currentUser)){
                return DtoUtil.returnFail("token失效，请重登录", "100000");
            }
            ItripHotelOrder order = itripHotelOrderService.getItripHotelOrderById(orderId);
            if (EmptyUtils.isEmpty(order)) {
                return DtoUtil.returnFail("100533", "没有查询到相应订单");
            }
            itripModifyHotelOrderVO = new ItripModifyHotelOrderVO();
            BeanUtils.copyProperties(order, itripModifyHotelOrderVO);
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("orderId", order.getId());
            List<ItripOrderLinkUserVo> itripOrderLinkUserList = itripOrderLinkUserService.getItripOrderLinkUserListByMap(param);
            itripModifyHotelOrderVO.setItripOrderLinkUserList(itripOrderLinkUserList);
            return DtoUtil.returnSuccess("获取订单成功", itripModifyHotelOrderVO);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100534");
        }
    }

    @ApiOperation(value = "修改订单的支付方式和状态", httpMethod = "POST", response = Dto.class)
    @RequestMapping(value = "/updateorderstatusandpaytype", method = RequestMethod.POST)
    @ResponseBody
    public Dto<Map<String, Boolean>> updateOrderStatusAndPayType(@RequestBody ItripModifyHotelOrderVO itripModifyHotelOrderVO, HttpServletRequest request) {
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if (null != currentUser && null != itripModifyHotelOrderVO) {
            try {
                ItripHotelOrder itripHotelOrder = new ItripHotelOrder();
                itripHotelOrder.setId(itripModifyHotelOrderVO.getId());
                //设置支付状态为：支付成功
                itripHotelOrder.setOrderStatus(2);
                itripHotelOrder.setPayType(itripModifyHotelOrderVO.getPayType());
                itripHotelOrder.setModifiedBy(currentUser.getId());
                itripHotelOrder.setModifyDate(new Date(System.currentTimeMillis()));
                itripHotelOrderService.itriptxModifyItripHotelOrder(itripHotelOrder);
            } catch (Exception e) {
                e.printStackTrace();
                return DtoUtil.returnFail("修改订单失败", "100522");
            }
            return DtoUtil.returnSuccess("修改订单成功");
        } else if (null != currentUser && null == itripModifyHotelOrderVO) {
            return DtoUtil.returnFail("不能提交空，请填写订单信息", "100523");
        } else {
            return DtoUtil.returnFail("token失效，请重新登录", "100000");
        }
    }
}
