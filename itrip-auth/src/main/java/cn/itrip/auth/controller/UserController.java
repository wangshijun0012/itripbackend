package cn.itrip.auth.controller;
        import cn.itrip.auth.serivice.UserService;
        import cn.itrip.beans.dto.Dto;
        import cn.itrip.beans.pojo.ItripUser;
        import cn.itrip.beans.vo.userinfo.ItripUserVO;
        import cn.itrip.common.DtoUtil;
        import cn.itrip.common.ErrorCode;
        import cn.itrip.common.MD5;
        import cn.itrip.common.SMSUtil;
        import io.swagger.annotations.Api;
        import io.swagger.annotations.ApiOperation;
        import org.springframework.stereotype.Controller;
        import org.springframework.web.bind.annotation.RequestBody;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestMethod;
        import org.springframework.web.bind.annotation.ResponseBody;

        import javax.annotation.Resource;
        import java.util.regex.Pattern;

/**
 * @author wangshijun
 */
@Api(value = "用户controller",tags = "用户操作接口")
@Controller
@RequestMapping("/api")
public class UserController {
    @Resource
    private UserService userService;
    @ApiOperation(value = "通过手机号注册",httpMethod = "POST",response = Dto.class,notes = "通过手机号进行注册")
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
    public Dto registerByPhone(@RequestBody ItripUserVO userVO){
        if(!validatePhone(userVO.getUserCode())){
            return DtoUtil.returnFail("请输入正确的手机号",ErrorCode.AUTH_ACTIVATE_FAILED);
        }
        ItripUser user = new ItripUser();
        user.setUserCode(userVO.getUserCode());
        user.setUserName(userVO.getUserName());
        user.setUserPassword(MD5.getMd5(userVO.getUserPassword(),32));
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
    private boolean validateMail(String email){
        String regex="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$"  ;
        return Pattern.compile(regex).matcher(email).find();
    }
    @RequestMapping(value = "/validatephone",method = RequestMethod.PUT)
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
    @RequestMapping(value = "/ckusr",method=RequestMethod.GET)
    @ResponseBody
    public Dto checkUser(String name){
        try {
            if(null == userService.findByUserCode(name)){
                return DtoUtil.returnSuccess("用户名可用");
            }else{
                return DtoUtil.returnFail("用户已存在，注册失败",ErrorCode.AUTH_USER_ALREADY_EXISTS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }
    }
    @RequestMapping(value ="/activate" ,method = RequestMethod.PUT)
    @ResponseBody
    public Dto activateMail(String user,String code){
        try {
            if(userService.validateMail(user,code)){
                return DtoUtil.returnSuccess("激活成功");
            }else{
                return DtoUtil.returnFail("激活失败",ErrorCode.AUTH_ACTIVATE_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @RequestMapping(value = "/doregister",method = RequestMethod.POST)
    @ResponseBody
    public Dto registerByMail(@RequestBody ItripUserVO userVO){
        if(!validateMail(userVO.getUserCode())){
            return DtoUtil.returnFail("邮箱地址不正确",ErrorCode.AUTH_PARAMETER_ERROR);
        }
        ItripUser user = new ItripUser();
        user.setUserCode(userVO.getUserCode());
        user.setUserName(userVO.getUserName());
        if(userService.findByUserCode(user.getUserCode()) != null){
            return DtoUtil.returnFail("邮箱已被注册",ErrorCode.AUTH_USER_ALREADY_EXISTS);
        }
        user.setUserPassword(MD5.getMd5(userVO.getUserPassword(),32));
        try {
            userService.createUserByMail(user);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("创建用户失败");
        }
        return DtoUtil.returnSuccess("发送邮箱成功");
    }

}
