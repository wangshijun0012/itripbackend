package cn.itrip.auth.serivice;

public interface SmsService {
    void sendSms(String to,String templateId,String[] datas);
}
