package cn.itrip.auth.service;

/**
 * @author wangshijun
 */
public interface SmsService {
    /**
     * 发送短信
     * @param to 接受者手机号码
     * @param templateId 短信模板id
     * @param datas 短信模板中的参数数组（模板1为例，arg1：手机验证码，arg2：过期时间【分钟】）
     */
    void sendSms(String to,String templateId,String[] datas);
}
