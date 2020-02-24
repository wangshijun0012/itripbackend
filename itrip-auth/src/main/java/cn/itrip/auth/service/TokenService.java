package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;

/**
 * @author wangshijun
 */
public interface TokenService {
    /**
     * 根据登录用户的信息和user-agent生成token的key
     * @param userAgent
     * @param user
     * @return
     */
    String generateToken(String userAgent , ItripUser user);

    /**
     * 从token的key中读取客户端类型并将登录用户的信息存放在token的value中，然后token保存在redis中。
     * @param token
     * @param user
     * @return
     * @throws Exception
     */
    Boolean savaToken(String token , ItripUser user) throws Exception;

    /**
     * 判断客户端上的token是否有效
     * @param userAgent
     * @param token
     * @return
     * @throws Exception
     */
    Boolean validateToken(String userAgent,String token) throws Exception;

    /**
     * 将对应的token进行删除并返回操作结果
     * @param token
     * @return
     * @throws Exception
     */
    Boolean delToken(String token) throws Exception;

    /**
     * 置换将要过期的token
     * @param userAgent
     * @param token
     * @return
     * @throws Exception
     */
    Boolean reloadToken(String userAgent,String token) throws Exception;
}
