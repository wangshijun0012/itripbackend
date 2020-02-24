package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisUtil;
import cn.itrip.common.UserAgentUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author wangshijun
 */
@Service
@Transactional
public class TokenServiceImpl implements TokenService {
    /**
     * "token"key的前缀（桌面端）
     */
    private static String TOKEN_PC = "token:PC-";
    /**
     * "token"key的前缀（手机端）
     */
    private static String TOKEN_MOBILE = "token:MOBIL-";
    /**
     * 根据user-agent生成的MD5长度
     */
    private static int USERAGENT_MD5_LENGTH = 6;
    /**
     * 根据userCode生成的MD5长度
     */
    private static int USERCODE_MD5_LENGTH = 32;
    /**
     * token保护时间（毫秒）
     */
    private static long TOKEN_PROTECT_LONG = 1000 * 60 * 5;
    /**
     * token失效时间（秒）
     */
    private static long TOKEN_EXPIR_SECOND = 60 * 60 * 2;
    /**
     * 旧token的失效时间（秒）
     */
    private static int OLD_TOKEN_EXPIR_SECOND = 60 * 10;
    @Resource
    private RedisUtil redisUtil;

    //token:PC-usercode(md5)-userid-creationdate-random(6)
    @Override
    public String generateToken(String userAgent, ItripUser user) {
        StringBuilder str = new StringBuilder();
        if (!UserAgentUtil.CheckAgent(userAgent)) {
            str.append(TOKEN_PC);
        } else {
            str.append(TOKEN_MOBILE);
        }
        str.append(MD5.getMd5(user.getUserCode(), USERCODE_MD5_LENGTH) + "-");
        str.append(user.getId() + "-");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        str.append(sdf.format(new Date()) + "-");
        //生成6位随机数
        str.append(MD5.getMd5(userAgent, USERAGENT_MD5_LENGTH));
        return str.toString();
    }

    @Override
    public Boolean savaToken(String token, ItripUser user) throws Exception {
        String json = JSONObject.toJSONString(user);
        //判断是哪种客户端
        if (token.startsWith(TOKEN_PC)) {
            //过期时间为两小时
            redisUtil.setString(token, json, TOKEN_EXPIR_SECOND);
        } else {
            //移动端永不过期
            redisUtil.setString(token, json);
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
        String agentMd5 = token.split("-")[4];
        if (!MD5.getMd5(userAgent, USERAGENT_MD5_LENGTH).equals(agentMd5)) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean delToken(String token) throws Exception {
        return redisUtil.del(token);
    }

    @Override
    public Boolean reloadToken(String userAgent, String token) throws Exception {
        //1.验证token是否有效
        if (!redisUtil.hasKey(token)) {
            throw new Exception("token无效");
        }
        //2.能不能置换
        Date genTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(token.split("-")[3]);
        long passed = Calendar.getInstance().getTimeInMillis() - genTime.getTime();
        if (passed < TOKEN_PROTECT_LONG) {
            throw new Exception("token置换保护期内，不能置换，剩余" + (TOKEN_PROTECT_LONG - passed) / 1000 + "秒");
        }
        //3.进行转换
        ItripUser user = JSON.parseObject(redisUtil.getString(token), ItripUser.class);
        String newToken = this.generateToken(userAgent, user);
        //4.老的token过期时间
        Boolean isDelay = redisUtil.setString(token, JSON.toJSONString(user), OLD_TOKEN_EXPIR_SECOND);
        //5.新的token保存至redis
        Boolean isSavad = this.savaToken(newToken, user);
        if (!isDelay || !isSavad) {
            throw new Exception("token操作失败");
        }
        return true;
    }
}