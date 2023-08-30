package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.R;
import javafx.scene.control.IndexRange;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

import static cn.itcast.hotel.constants.HotelConstans.MAPPING_TEMPLATE;
@SpringBootTest
public class HotelIndexTest {
    @Autowired
    private IHotelService hotelService;

    private RestHighLevelClient client;


    @Test
    void testInit() {
        System.out.println(client);
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

   @Test
    void createHotelIndex() throws IOException {
        //1.创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        //2.准备请求的参数：DSL语句
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        //3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteHotelIndex() throws IOException {
        //1.创建Request对象
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        //2.发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);

    }

    @Test
    void testExistsHotelIndex() throws IOException {
        //1.创建Request对象
        GetIndexRequest request = new GetIndexRequest("hotel");
        //2.发送请求
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        //3.输出
        System.err.println(exists ? "索引库已经存在!" : "索引库不存在!");
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
