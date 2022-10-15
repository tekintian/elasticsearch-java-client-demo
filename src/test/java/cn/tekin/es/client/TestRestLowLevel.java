package cn.tekin.es.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST低级客户端 演示
 *
 * @author tekintian@gmail.com
 * @version v0.0.1
 * @since v0.0.1 2022-10-15 17:30
 */
public class TestRestLowLevel {
    private static final ObjectMapper objMapper = new ObjectMapper();
    private RestClient restClient;

    /**
     * 初始化ES链接
     */
    @Before
    public void init() {
        //集群链接
//        RestClientBuilder restClientBuilder = RestClient.builder(
//                new HttpHost("192.168.2.8", 9200, "http"),
//                new HttpHost("192.168.2.8", 9201, "http"),
//                new HttpHost("192.168.2.8", 9202, "http"));

        //单机链接
        RestClientBuilder restClientBuilder =
                RestClient.builder(new HttpHost("192.168.2.8", 9200));

        restClientBuilder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                System.out.println("出错了 -> " + node);
            }
        });
        this.restClient = restClientBuilder.build();
    }

    /**
     * 关闭ES链接
     *
     * @throws IOException
     */
    @After
    public void after() throws IOException {
        restClient.close();
    }

    // 查询集群状态
    @Test
    public void testGetInfo() throws IOException {
        Request request = new Request("GET", "/_cluster/state");
        request.addParameter("pretty", "true");
        Response response = this.restClient.performRequest(request);
        System.out.println(response.getStatusLine());
        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    // 新增数据
    @Test
    public void testCreateData() throws IOException {
        Request esreq = new Request("POST","/mydemo/house/202210");
        Map<String,Object> data = new HashMap<>();
        data.put("id","202210");
        data.put("title","Test From Java RestClient! 202210");
        data.put("price","202210");

        esreq.setJsonEntity(objMapper.writeValueAsString(data));
        try {
            Response resp = this.restClient.performRequest(esreq);
            String result = String.format("Es StatusLine %s \r\n Entity: %s "
                    , resp.getStatusLine(), resp.getEntity());

            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 根据id查询数据
    @Test
    public void testQueryData() throws IOException {
        Request request = new Request("GET", "/mydemo/house" +
                "/202210/_source");
        Response response = this.restClient.performRequest(request);
        System.out.println(response.getStatusLine());
        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    // 搜索数据
    @Test
    public void testSearchData() throws IOException {
        Request request = new Request("POST", "/mydemo/house/_search");
        String searchJson = "{\"query\": {\"match\": {\"title\": \"拎包入住\"}}}";
        request.setJsonEntity(searchJson);
        request.addParameter("pretty", "true");
        Response response = this.restClient.performRequest(request);
        System.out.println(response.getStatusLine());
        System.out.println(EntityUtils.toString(response.getEntity()));
    }
}
