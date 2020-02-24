package cn.itrip.search.pojo;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.stereotype.Component;

@Component
@Data
public class Hotel {
    @Field("cityId")
    private Long cityId;
    @Field("details")
    private String details;
    @Field("hotelName")
    private String hotelName;

}
