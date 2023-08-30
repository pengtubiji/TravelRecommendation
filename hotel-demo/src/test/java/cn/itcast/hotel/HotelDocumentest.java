package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static cn.itcast.hotel.constants.HotelConstans.MAPPING_TEMPLATE;

@SpringBootTest
public class HotelDocumentest {
    @Autowired
    private IHotelService hotelService;

    private RestHighLevelClient client;


    @Test
    void testInit() {
        System.out.println(client);
    }

    @Test
    void testAddDocument() throws IOException {
        //根据id查询酒店数据
        Hotel hotel = hotelService.getById(61083L);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        //1.准备Request对象
        IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
        //2.准备Json文档
        request.source(JSON.toJSONString(hotelDoc),XContentType.JSON);
        //3.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDocmentById() throws IOException {
        //1.准备Request
        GetRequest request = new GetRequest("hotel","61083");
        //2.发送请求，得到响应
        GetResponse response = client.get(request,RequestOptions.DEFAULT);
        //3.解析响应结果
        String json = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json,HotelDoc.class);
        System.out.println(hotelDoc);
    }

    @Test
    void testBulkRequest() throws IOException {
        //批量查询酒店数据
        List<Hotel> hotels = hotelService.list();
        //1.创建Request
        BulkRequest request = new BulkRequest();
        //2.准备参数，添加多个新增的Request
        for(Hotel hotel : hotels){
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        }
        //3.发送请求
        client.bulk(request,RequestOptions.DEFAULT);
    }

    @Test
    void testUpdateDocument() throws IOException {
        //1.准备Request
         UpdateRequest request = new UpdateRequest();
        //2.准备请求参数
        request.doc(
                "price","952",
                "starName","四钻"
        );
        //3.发送请求
        client.update(request,RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteDocument() throws  IOException{
        //1.准备Request
        DeleteRequest request = new DeleteRequest("hotel","61083");
        //2.发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }


    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.10.100:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }
}
