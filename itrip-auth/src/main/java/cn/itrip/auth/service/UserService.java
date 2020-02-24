package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;

/**
 * @author wangshijun
 */
public interface UserService {
    /**
     * 根据用户账号名查询用户记录
     * @param userCode 用户账户名
     * @return
     */
    ItripUser findByUserCode(String userCode);

    /**
     * 将用户登录信息进行密码的加密，以便存入数据库
     * @param name 用户昵称
     * @param password 用户密码
     * @return 返回密码已加密好的用户对象
     * @throws Exception
     */
    ItripUser login(String name,String password) throws Exception;

    /**
     * 根据注册的用户账号名（手机号）进行用户信息的注册处理流程
     * @param user
     * @throws Exception
     */
    void createUserByPhone(ItripUser user) throws Exception;

    /**
     * 根据注册的用户账号名（邮箱地址）进行用户信息的注册处理流程
     * @param user
     * @throws Exception
     */
    void createUserByMail(ItripUser user) throws Exception;

    /**
     * 判断注册用户收到的手机验证码和用户输入的验证码是否一致
     * @param phoneNum 用户的手机号码
     * @param code 用户输入的验证码
     * @return
     * @throws Exception
     */
    boolean validatePhone(String phoneNum,String code) throws Exception;

    /**
     * 判断注册用户收到的邮箱验证码和用户输入的验证码是否一致
     * @param mail 用户的邮箱地址
     * @param code 用户输入的验证码
     * @return
     * @throws Exception
     */
    boolean validateMail(String mail,String code) throws  Exception;

}
