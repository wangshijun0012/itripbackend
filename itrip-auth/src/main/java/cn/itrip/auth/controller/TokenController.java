package cn.itrip.auth.controller;

import cn.itrip.auth.service.TokenService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author wangshijun
 */
@Api(value = "token处理接口" ,tags = "token处理接口")
@Controller
@RequestMapping("/api")
public class TokenController {
    @Resource
    private TokenService tokenService;
    @ApiOperation(value = "token置换接口",response = DtoUtil.class,httpMethod = "POST")
    @RequestMapping(value = "/retoken",method = RequestMethod.POST,headers = "token")
    @ResponseBody
    public Dto reloadToken(HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        try {
            Boolean isReloadToken = tokenService.reloadToken(userAgent, token);
            if(!isReloadToken){
                return DtoUtil.returnFail("置换失败", ErrorCode.AUTH_TOKEN_INVALID);
            }
            return DtoUtil.returnSuccess("置换成功",token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DtoUtil.returnFail("置换错误",ErrorCode.AUTH_UNKNOWN);
    }
}
