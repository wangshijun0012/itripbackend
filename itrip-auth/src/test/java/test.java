import cn.itrip.common.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-redis.xml")
public class test {
    @Resource
    private RedisUtil redisUtil;
    @Test
    public void test01(){

        String str = redisUtil.getString("hello");
        System.out.println(str);


    }
}
