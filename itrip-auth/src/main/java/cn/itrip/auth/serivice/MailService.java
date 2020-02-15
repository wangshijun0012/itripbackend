package cn.itrip.auth.serivice;

/**
 * @author wangshijun
 */
public interface MailService {
    /**
     * 发送邮件
     * @param mailAddr 收信人的邮箱地址
     * @param code 收信内容（内含邮箱验证码）
     */
    void sendMail(String mailAddr,String code);
}
