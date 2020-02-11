package cn.itrip.auth.serivice;

import cn.itrip.beans.pojo.ItripUser;

public interface UserService {
    ItripUser findByUserCode(String userCode);
    ItripUser login(String name,String password) throws Exception;
}
