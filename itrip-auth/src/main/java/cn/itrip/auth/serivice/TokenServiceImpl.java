package cn.itrip.auth.serivice;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisUtil;
import cn.itrip.common.UserAgentUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
@Service
public class TokenServiceImpl implements TokenService {
    //token:PC-usercode(md5)-userid-creationdate-random(6)
    @Override
    public String generateToken(String userAgent , ItripUser user) {
        StringBuilder str = new StringBuilder("token:");
        if(!UserAgentUtil.CheckAgent(userAgent)){
            str.append("PC-");
        }else{
            str.append("MOBILE-");
        }
        str.append(MD5.getMd5(user.getUserCode(),32) + "-");
        str.append(user.getId()+ "-");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        str.append(sdf.format(new Date()) + "-");
        //生成6位随机数
        str.append(MD5.getMd5(userAgent,6));
        return str.toString();
    }
    @Resource
    private RedisUtil redisUtil;
    @Override
    public Boolean savaToken(String token, ItripUser user) throws Exception {
        String json = JSONObject.toJSONString(user);
        //判断是那种客户端
        if(token.startsWith("token:PC-")){
            redisUtil.setString(token,json,2*60*60);//过期时间为两小时
        }else{
            redisUtil.setString(token,json);//移动端永不过期
        }
        return true;
    }
}
