package cn.itrip.auth.serivice;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisUtil;
import cn.itrip.dao.user.ItripUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author wangshijun
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Resource
    private ItripUserMapper itripUserMapper;
    @Resource
    private MailService mailService;
    @Resource
    private SmsService smsService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public ItripUser findByUserCode(String userCode) {
        HashMap map = new HashMap(1);
        map.put("userCode", userCode);
        try {
            List<ItripUser> list = itripUserMapper.getItripUserListByMap(map);
            if (list.size() > 0) {
                return list.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ItripUser login(String name, String password) throws Exception {
        ItripUser user = findByUserCode(name);
        if (user != null && user.getUserPassword().equals(password)) {
            if (user.getActivated() == 0) {
                throw new Exception("用户未激活");
            }
            return user;
        }
        return null;
    }

    @Override
    public void createUserByPhone(ItripUser user) throws Exception {
        //生成一个用户
        itripUserMapper.insertItripUser(user);
        //生成4位验证码
        int code = MD5.getRandomCode();
        smsService.sendSms(user.getUserCode(), "1", new String[]{String.valueOf(code), "10"});
        //保存到redis
        redisUtil.setString("activation:" + user.getUserCode(), String.valueOf(code), 600);
    }

    @Override
    public void createUserByMail(ItripUser user) throws Exception {
        //保存到数据库
        itripUserMapper.insertItripUser(user);
        //生成激活码
        String code = MD5.getMd5(new Date().toString(), 32);
        //发送邮件
        mailService.sendMail(user.getUserCode(), code);
        //保存到redis
        redisUtil.setString("activation:" + user.getUserCode(), code, 600);
    }

    @Override
    public boolean validatePhone(String phoneNum, String code) throws Exception {
        String value = redisUtil.getString("activation:" + phoneNum);
        if (value != null && value.equals(code)) {
            ItripUser user = findByUserCode(phoneNum);
            if (user != null) {
                user.setActivated(1);
                user.setFlatID(user.getId());
                user.setUserType(0);
                itripUserMapper.updateItripUser(user);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validateMail(String mail, String code) throws Exception {
        String value = redisUtil.getString("activation:" + mail);
        if (value != null && value.equals(code)) {
            ItripUser user = findByUserCode(mail);
            user.setActivated(1);
            user.setFlatID(user.getId());
            user.setUserType(0);
            itripUserMapper.updateItripUser(user);
            return true;
        }
        return false;
    }
}
