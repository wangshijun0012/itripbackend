import cn.itrip.search.pojo.Hotel;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class TestSolr {
    @Test
    public void test01() throws IOException, SolrServerException {
        //获取一个客户端
        String url = "http://localhost:8080/solr/hotel";
        HttpSolrClient client = new HttpSolrClient.Builder(url).withConnectionTimeout(10000).withSocketTimeout(60000).build();
        //查询条件的设置
        SolrQuery query = new SolrQuery("*:*");//全查询

        query.addFilterQuery("cityId:"+2);//添加其他的查询条件

        //分页
        query.setStart(0);
        query.setRows(3);
        //查询  得到结果
        QueryResponse response = client.query(query);
        //获取查询到的数据
        List<Hotel> list = response.getBeans(Hotel.class);
        System.out.println(list);
    }
}
