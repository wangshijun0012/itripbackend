package cn.itrip.auth.serivice;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisUtil;
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
    @Resource
    private SmsService smsService;
    @Resource
    private RedisUtil redisUtil;
    @Override
    public void createUserByPhone(ItripUser user) throws Exception {
        //生成一个用户
        itripUserMapper.insertItripUser(user);
        //生成4位验证码
        int code = MD5.getRandomCode();
        smsService.sendSms(user.getUserCode(),"1",new String[]{String.valueOf(code),"10"});
        //保存到redis
        redisUtil.setString("activation:" + user.getUserCode(),String.valueOf(code),600);
    }
    @Override
    public boolean validatePhone(String phoneNum,String code) throws Exception{
        String value = redisUtil.getString("activation:" + phoneNum);
        if(value != null && value.equals(code)){
            ItripUser user = findByUserCode(phoneNum);
            if(user != null){
                user.setActivated(1);
                user.setFlatID(user.getId());
                user.setUserType(0);
                itripUserMapper.updateItripUser(user);
                return true;
            }
        }
        return false;
    }


}
