import cn.itrip.common.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-redis.xml")
public class test {
    @Resource
    private RedisUtil redisUtil;
    @Test
    public void test01(){

        String str = redisUtil.getString("token:PC-ed6e201becad0e79ae04178e519fd13b-29-20200305135744-37a36c");
        System.out.println(str);


    }
}
