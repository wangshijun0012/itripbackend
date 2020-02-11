package cn.itrip.auth.serivice;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.dao.user.ItripUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private ItripUserMapper itripUserMapper;
    @Override
    public ItripUser findByUserCode(String userCode) {
        HashMap map = new HashMap();
        map.put("userCode",userCode);
        try {
            List<ItripUser> list = itripUserMapper.getItripUserListByMap(map);
            if(list.size() >0){
                return list.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public ItripUser login(String name,String password) throws Exception {
        ItripUser user = findByUserCode(name);
        if(user != null && user.getUserPassword().equals(password)){
            if(user.getActivated() == 0){
                throw new Exception("用户未激活");
            }
            return user;
        }
        return null;
    }
}
