package cn.itrip.auth.controller;
        import cn.itrip.auth.serivice.UserService;
        import cn.itrip.beans.dto.Dto;
        import cn.itrip.beans.pojo.ItripUser;
        import cn.itrip.beans.vo.userinfo.ItripUserVO;
        import cn.itrip.common.DtoUtil;
        import cn.itrip.common.ErrorCode;
        import cn.itrip.common.MD5;
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
@Api(value = "用户controller",tags = "用户信息操作接口")
@Controller
@RequestMapping("/api")
public class UserController {
    @Resource
    private UserService userService;
    @ApiOperation(value = "手机号注册",response = DtoUtil.class,httpMethod = "POST")
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
    @ApiOperation(value = "手机号格式判断",notes = "返回boolean类型")
    private Boolean validatePhone(String phoneNum){
        String reg = "^1[356789]\\d{9}$";
        return Pattern.compile(reg).matcher(phoneNum).find();
    }
    @ApiOperation(value = "邮箱地址格式判断",notes = "返回boolean类型")
    private boolean validateMail(String email){
        String regex="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$"  ;
        return Pattern.compile(regex).matcher(email).find();
    }
    @ApiOperation(value = "手机验证码验证",response = DtoUtil.class,httpMethod = "PUT",notes = "验证成功则激活账户")
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
    @ApiOperation(value = "用户名验证",response = DtoUtil.class,httpMethod = "GET")
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
    @ApiOperation(value = "进行邮箱验证码验证",response = DtoUtil.class,httpMethod = "PUT",notes = "验证成功则激活账户")
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
    @ApiOperation(value = "邮箱地址注册",response = DtoUtil.class,httpMethod = "POST")
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
