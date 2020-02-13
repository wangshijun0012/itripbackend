package cn.itrip.auth.controller;

        import cn.itrip.auth.serivice.TokenService;
        import cn.itrip.auth.serivice.UserService;
        import cn.itrip.beans.dto.Dto;
        import cn.itrip.beans.pojo.ItripUser;
        import cn.itrip.beans.vo.ItripTokenVO;
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
        import javax.servlet.http.HttpServletRequest;
        import java.lang.reflect.Array;
        import java.util.ArrayList;
        import java.util.Calendar;

@Controller
@RequestMapping("/api")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private TokenService tokenService;
    @RequestMapping(value = "/dologin",method = RequestMethod.POST)
    @ResponseBody
    public Dto dologin(String name , String password, HttpServletRequest request) {
        ItripUser user = null;
        try {
            user = userService.login(name, MD5.getMd5(password,32));
            if(user == null){
                return DtoUtil.returnFail("用户名密码错误", ErrorCode.AUTH_AUTHENTICATION_FAILED);
            }
            //获得浏览器请求头中的User-Agent
            String userAgent =request.getHeader("user-agent");
            //登陆成功，生成token
            String token = tokenService.generateToken(userAgent,user);
            //保存token到redis
            tokenService.savaToken(token,user);
            //返回一个vo对象
            ItripTokenVO vo = new ItripTokenVO(token, Calendar.getInstance().getTimeInMillis() + 2*60*60 ,Calendar.getInstance().getTimeInMillis());
            return DtoUtil.returnSuccess("登陆成功",vo);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }
    }
    @RequestMapping(value = "/logout",method = RequestMethod.POST,headers = "token")
    @ResponseBody
    public Dto logout(HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        try {
            if(tokenService.validateToken(userAgent,token)){
                tokenService.delToken(token);
                return DtoUtil.returnSuccess("退出成功");
            }else{
                return DtoUtil.returnFail("token无效",ErrorCode.AUTH_TOKEN_INVALID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DtoUtil.returnFail("退出失败",ErrorCode.AUTH_PARAMETER_ERROR);
    }
    @RequestMapping(value = "/reloadToken",method = RequestMethod.POST,headers = "token")
    @ResponseBody
    public Dto reloadToken(HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        try {
            Boolean isReloadToken = tokenService.reloadToken(userAgent, token);
            if(!isReloadToken){
                return DtoUtil.returnFail("置换失败",ErrorCode.AUTH_TOKEN_INVALID);
            }
            return DtoUtil.returnSuccess("置换成功",token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DtoUtil.returnFail("置换错误",ErrorCode.AUTH_UNKNOWN);
    }
    @RequestMapping(value = "/doLoginMessage",method = RequestMethod.POST)
    @ResponseBody
    public Dto sendLoginMessage(String mobilPhoneNum,String data1,String data2){
        boolean isSended = SMSUtil.sendSMS(mobilPhoneNum,"1", new String[]{data1,data2});
        if(isSended){
            return DtoUtil.returnSuccess("发送成功");
        }else{
            return DtoUtil.returnFail("发送失败",ErrorCode.AUTH_UNKNOWN);
        }
    }
}
