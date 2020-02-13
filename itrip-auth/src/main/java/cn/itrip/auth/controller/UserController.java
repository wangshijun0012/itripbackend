package cn.itrip.auth.controller;
        import cn.itrip.auth.serivice.UserService;
        import cn.itrip.beans.dto.Dto;
        import cn.itrip.beans.pojo.ItripUser;
        import cn.itrip.beans.vo.userinfo.ItripUserVO;
        import cn.itrip.common.DtoUtil;
        import cn.itrip.common.ErrorCode;
        import cn.itrip.common.MD5;
        import cn.itrip.common.SMSUtil;
        import org.springframework.stereotype.Controller;
        import org.springframework.web.bind.annotation.RequestBody;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestMethod;
        import org.springframework.web.bind.annotation.ResponseBody;

        import javax.annotation.Resource;
        import java.util.regex.Pattern;

@Controller
@RequestMapping("/api")
public class UserController {
    @Resource
    private UserService userService;
    @RequestMapping(value = "/doLoginMessage",method = RequestMethod.POST)
    @ResponseBody
    //参数1：接受验证码的手机号，参数2：验证码内容，参数3：验证码有效时间（分钟）
    public Dto sendLoginMessage(String mobilPhoneNum,String data1,String data2){
        boolean isSended = SMSUtil.sendSMS(mobilPhoneNum,"1", new String[]{data1,data2});
        if(isSended){
            return DtoUtil.returnSuccess("发送成功");
        }else{
            return DtoUtil.returnFail("发送失败",ErrorCode.AUTH_UNKNOWN);
        }
    }
    @RequestMapping(value = "/registerbyphone" ,method = RequestMethod.POST)
    @ResponseBody
    public Dto registerByPhone(@RequestBody ItripUserVO vo){
        if(!validatePhone(vo.getUserCode())){
            return DtoUtil.returnFail("请输入正确的手机号",ErrorCode.AUTH_ACTIVATE_FAILED);
        }
        ItripUser user = new ItripUser();
        user.setUserCode(vo.getUserCode());
        user.setUserName(vo.getUserName());
        user.setUserPassword(MD5.getMd5(vo.getUserPassword(),32));
        try {
            if(userService.findByUserCode(user.getUserCode()) != null){
                return DtoUtil.returnFail("用户已存在",ErrorCode.AUTH_ILLEGAL_USERCODE);
            }
            userService.createUserByPhone(user);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送验证码失败");
        }
        return DtoUtil.returnSuccess("发送验证码成功",user);
    }
    private Boolean validatePhone(String phoneNum){
        String reg = "^1[356789]\\d{9}$";
        return Pattern.compile(reg).matcher(phoneNum).find();
    }
    @RequestMapping(value = "/validatephone",method = RequestMethod.POST)
    @ResponseBody
    public Dto validatePhone(String user,String code){
        try {
            if(userService.validatePhone(user, code)){
                return DtoUtil.returnSuccess("验证成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DtoUtil.returnFail("验证失败",ErrorCode.AUTH_ACTIVATE_FAILED);
    }
}
