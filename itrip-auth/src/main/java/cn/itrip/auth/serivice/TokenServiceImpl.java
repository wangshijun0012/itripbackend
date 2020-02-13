package cn.itrip.auth.serivice;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisUtil;
import cn.itrip.common.UserAgentUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonEncoding;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class TokenServiceImpl implements TokenService {
    //token:PC-usercode(md5)-userid-creationdate-random(6)
    @Override
    public String generateToken(String userAgent, ItripUser user) {
        StringBuilder str = new StringBuilder("token:");
        if (!UserAgentUtil.CheckAgent(userAgent)) {
            str.append("PC-");
        } else {
            str.append("MOBILE-");
        }
        str.append(MD5.getMd5(user.getUserCode(), 32) + "-");
        str.append(user.getId() + "-");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        str.append(sdf.format(new Date()) + "-");
        //生成6位随机数
        str.append(MD5.getMd5(userAgent, 6));
        return str.toString();
    }

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Boolean savaToken(String token, ItripUser user) throws Exception {
        String json = JSONObject.toJSONString(user);
        //判断是哪种客户端
        if (token.startsWith("token:PC-")) {
            redisUtil.setString(token, json, 2 * 60 * 60);//过期时间为两小时
        } else {
            redisUtil.setString(token, json);//移动端永不过期
        }
        return true;
    }

    @Override
    public Boolean validateToken(String userAgent, String token) throws Exception {
        //判断redis中token是否存在
        if (!redisUtil.hasKey(token)) {
            return false;
        }
        //token后六位和前端传过来的userAgent是否一致,一定程度上可以防止盗用token进行非法操作
        String agentMD5 = token.split("-")[4];
        if (!MD5.getMd5(userAgent, 6).equals(agentMD5)) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean delToken(String token) throws Exception {
        return  redisUtil.del(token);
    }

    @Override
    public Boolean reloadToken(String userAgent,String token) throws Exception {
        //1.验证token是否有效
        if(!redisUtil.hasKey(token)){
            throw new Exception("token无效");
        }
        //2.能不能置换
        Date genTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(token.split("-")[3]);
        long passed = Calendar.getInstance().getTimeInMillis() - genTime.getTime();
        if(passed < 1000*60*5){
            throw new Exception("token置换保护期内，不能置换，剩余" + (1000*60*5 - passed)/1000 + "秒");
        }
        //3.进行转换
        ItripUser user = JSON.parseObject(redisUtil.getString(token), ItripUser.class);
        String newToken = this.generateToken(userAgent,user);
        //4.老的token过期时间
        Boolean isDelay = redisUtil.setString(token,JSON.toJSONString(user),300);
        //5.新的token保存至redis
        Boolean isSavad = this.savaToken(newToken, user);
        if(!isDelay || !isSavad){
            throw new Exception("token操作失败");
        }
        return true;
    }
}