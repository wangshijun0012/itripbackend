package cn.itrip.auth.serivice;

import cn.itrip.beans.pojo.ItripUser;

public interface TokenService {
    String generateToken(String userAgent , ItripUser user);
    Boolean savaToken(String token , ItripUser user) throws Exception;
    Boolean validateToken(String userAgent,String token) throws Exception;
    Boolean delToken(String token) throws Exception;
    Boolean reloadToken(String userAgent,String token) throws Exception;
}
