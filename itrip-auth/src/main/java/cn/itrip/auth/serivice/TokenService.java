package cn.itrip.auth.serivice;

import cn.itrip.beans.pojo.ItripUser;

public interface TokenService {
    String generateToken(String userAgent , ItripUser user);
    Boolean savaToken(String token , ItripUser user) throws Exception;
}
