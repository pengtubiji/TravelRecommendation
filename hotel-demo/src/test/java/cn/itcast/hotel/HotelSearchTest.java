package cn.itcast.hotel;

import cn.itcast.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.apache.lucene.index.Term;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static cn.itcast.hotel.constants.HotelConstans.MAPPING_TEMPLATE;

@SpringBootTest
public class HotelSearchTest {
    @Autowired
    private IHotelService hotelService;

    private RestHighLevelClient client;


    @Test
    void testInit() {
        System.out.println(client);
    }

    @Test
    void testAggregation() throws IOException {
        //1.准备Request
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL
        //2.1设置size
        request.source().size(0);
        //2.2聚合
        request.source().aggregation(AggregationBuilders
        .terms("brandAgg")
        .field("brand")
        .size(10)
        );
        //3.发起请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.解析结果
        Aggregations aggregations = response.getAggregations();
        //4.1根据聚合名称获取聚合结果
        Terms brandTerms = aggregations.get("brandAgg");
        //4.2获取buckets
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        //4.3遍历
        for (Terms.Bucket bucket : buckets){
            //4.4获取key
            String key = bucket.getKeyAsString();
            System.out.println(key);
        }
    }

    @Test
    void testSuggest() throws IOException {
        //1.准备Request
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL
        request.source().suggest(new SuggestBuilder().addSuggestion(
           "suggestions",
           SuggestBuilders.completionSuggestion("suggestion")
           .prefix("hz")
           .skipDuplicates(true)
           .size(10)
        ));
        //3.发起请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.解析结果
        Suggest suggest = response.getSuggest();
        //4.1根据补全查询名称，获取补全结果
        CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
        //4.2获取options
        List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
        //4.3遍历
        for(CompletionSuggestion.Entry.Option option :options){
            String text = option.getText().toString();
            System.out.println(text);
        }
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
