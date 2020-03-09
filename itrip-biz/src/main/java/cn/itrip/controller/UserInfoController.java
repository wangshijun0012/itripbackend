package cn.itrip.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.pojo.ItripUserLinkUser;
import cn.itrip.beans.vo.userinfo.ItripAddUserLinkUserVO;
import cn.itrip.beans.vo.userinfo.ItripModifyUserLinkUserVO;
import cn.itrip.beans.vo.userinfo.ItripSearchUserLinkUserVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.ValidationToken;
import cn.itrip.service.orderlinkuser.ItripOrderLinkUserServiceImpl;
import cn.itrip.service.userlinkuser.ItripUserLinkUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@Api(value = "用户信息接口",tags = "用户信息接口")
@RequestMapping(value="/api/userinfo")
public class UserInfoController {
    @Resource
    private ItripUserLinkUserService itripUserLinkUserService;

    @Resource
    private ValidationToken validationToken;

    @Resource
    private ItripOrderLinkUserServiceImpl itripOrderLinkUserService;

    /**
     * 根据UserId,联系人姓名查询常用联系人-add by donghai
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "查询常用联系人接口", httpMethod = "POST", response = Dto.class)
    @RequestMapping(value = "/queryuserlinkuser",method= RequestMethod.POST)
    @ResponseBody
    public Dto<ItripUserLinkUser> queryUserLinkUser(@RequestBody ItripSearchUserLinkUserVO itripSearchUserLinkUserVO, HttpServletRequest request){
       if(EmptyUtils.isEmpty(request.getHeader("token")) || !validationToken.getRedisAPI().hasKey(request.getHeader("token"))){
           return DtoUtil.returnFail("token失效，请重新登录","100000");
       }
       ItripUser user = validationToken.getCurrentUser(request.getHeader("token"));
        HashMap<String , Object> map = new HashMap<>();
        map.put("userId",user.getId());
        map.put("linkUserName", EmptyUtils.isEmpty(itripSearchUserLinkUserVO.getLinkUserName()) ? null : itripSearchUserLinkUserVO.getLinkUserName());
        List<ItripUserLinkUser> list = null;
        try {
            list = itripUserLinkUserService.getItripUserLinkUserListByMap(map);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取常用联系人信息失败","100401");
        }
            return DtoUtil.returnDataSuccess(list);
    }

    @ApiOperation(value = "新增常用联系人接口", httpMethod = "POST", response = Dto.class)
    @RequestMapping(value="/adduserlinkuser",method=RequestMethod.POST)
    @ResponseBody
    public Dto<Object> addUserLinkUser(@RequestBody ItripAddUserLinkUserVO itripAddUserLinkUserVO, HttpServletRequest request){
        String tokenString  = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(null != currentUser && null != itripAddUserLinkUserVO){
            ItripUserLinkUser itripUserLinkUser = new ItripUserLinkUser();
            BeanUtils.copyProperties(itripAddUserLinkUserVO,itripUserLinkUser);
            itripUserLinkUser.setUserId(currentUser.getId());
            itripUserLinkUser.setCreatedBy(currentUser.getId());
            itripUserLinkUser.setCreationDate(new Date(System.currentTimeMillis()));
            try {
                itripUserLinkUserService.addItripUserLinkUser(itripUserLinkUser);
            } catch (Exception e) {
                e.printStackTrace();
                return DtoUtil.returnFail("新增常用联系人失败", "100411");
            }
            return DtoUtil.returnSuccess("新增常用联系人成功");
        }else if(null != currentUser && null == itripAddUserLinkUserVO){
            return DtoUtil.returnFail("不能提交空，请填写常用联系人信息","100412");
        }else{
            return DtoUtil.returnFail("token失效，请重新登录", "100000");
        }
    }

    @ApiOperation(value = "修改常用联系人接口", httpMethod = "POST",response = Dto.class)
    @RequestMapping(value="/modifyuserlinkuser",method=RequestMethod.POST)
    @ResponseBody
    public Dto<Object> updateUserLinkUser(@RequestBody ItripModifyUserLinkUserVO itripModifyUserLinkUserVO, HttpServletRequest request){
        String tokenString  = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(null != currentUser && null != itripModifyUserLinkUserVO){
            ItripUserLinkUser itripUserLinkUser = new ItripUserLinkUser();
            BeanUtils.copyProperties(itripModifyUserLinkUserVO,itripUserLinkUser);
            itripUserLinkUser.setUserId(currentUser.getId());
            itripUserLinkUser.setModifiedBy(currentUser.getId());
            itripUserLinkUser.setModifyDate(new Date(System.currentTimeMillis()));

            try {
                itripUserLinkUserService.modifyItripUserLinkUser(itripUserLinkUser);
            } catch (Exception e) {
                e.printStackTrace();
                return DtoUtil.returnFail("修改常用联系人失败", "100421");
            }
            return DtoUtil.returnSuccess("修改常用联系人成功");
        }else if(null != currentUser && null == itripModifyUserLinkUserVO){
            return DtoUtil.returnFail("不能提交空，请填写常用联系人信息","100422");
        }else{
            return DtoUtil.returnFail("token失效，请重新登录", "100000");
        }
    }

    @ApiOperation(value = "删除常用联系人接口", httpMethod = "GET",response = Dto.class)
    @RequestMapping(value="/deluserlinkuser",method=RequestMethod.GET)
    @ResponseBody
    public Dto<Object> delUserLinkUser(Long[] ids, HttpServletRequest request) {
        String tokenString  = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        List<Long> idsList = new ArrayList<Long>();
        if(null != currentUser && EmptyUtils.isNotEmpty(ids)){
            try {
                List<Long> linkUserIds = itripOrderLinkUserService.getItripOrderLinkUserIdsByOrder();
                //用于所有指定元素添加到指定的集合，即将ids数组中的元素依次追加进idsList集合中
                Collections.addAll(idsList, ids);
                //取得两个List的交集,并存放在idsList集合中
                idsList.retainAll(linkUserIds);
                if(idsList.size() > 0)
                {
                    return DtoUtil.returnFail("所选的常用联系人中有与某条待支付的订单关联的项，无法删除","100431");
                }else{
                    itripUserLinkUserService.deleteItripUserLinkUserByIds(ids);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return DtoUtil.returnFail("删除常用联系人失败","100432");
            }
            return DtoUtil.returnSuccess("删除常用联系人成功");
        }else if(null != currentUser && EmptyUtils.isEmpty(ids)){
            return DtoUtil.returnFail("请选择要删除的常用联系人","100433");
        }else{
            return DtoUtil.returnFail("token失效，请重新登录","100000");
        }
    }

}
