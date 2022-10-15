# Elasticsearch JAVA Client 的三种客户端演示



​          Elasticsearch提供了2种REST客户端和一个Api客户端，一种是低级客户端，一种是高级客户端，API客户端为Spring Data Elasticsearch。

- Java Low Level REST Client:官方提供的低级客户端。该客户端通过http来连接Elasticsearch集群。用户在 使用该客户端时需要将请求数据手动拼接成Elasticsearch所需JSON格式进行发送，收到响应时同样也需要将 返回的JSON数据手动封装成对象。虽然麻烦，不过该客户端兼容所有的Elasticsearch版本。
- Java High Level REST Client:官方提供的高级客户端。该客户端基于低级客户端实现，它提供了很多便捷的 API来解决低级客户端需要手动转换数据格式的问题。
- Spring Data Elasticsearch 客户端，这个使用的是API的模式



这里需要说明的是 Spring Data Elasticsearch 客户端这个需要严格和使用的Springboot 版本和ES服务端对应。详细常见对照表， 其他2种REST客户端则是根据你导入的ES包的版本来绑定你的ES服务端。



3种客户端的es CRUD和 查询测试代码全部在 src/test/java/cn/tekin/es/client 包下面



###构造数据

~~~sh
POST http://127.0.0.1:9200/mydemo/house/_bulk
{"index":{"_index":"mydemo","_type":"house"}}
{"id":"1001","title":"整租 · 南丹大楼 1居室 7500","price":"7500"}
{"index":{"_index":"mydemo","_type":"house"}}
{"id":"1002","title":"陆家嘴板块，精装设计一室一厅，可拎包入住诚意租。","price":"8500"}
{"index":{"_index":"mydemo","_type":"house"}}
{"id":"1003","title":"整租 · 健安坊 1居室 4050","price":"7500"}
{"index":{"_index":"mydemo","_type":"house"}}
{"id":"1004","title":"整租 · 中凯城市之光+视野开阔+景色秀丽+拎包入住","price":"6500"}
{"index":{"_index":"mydemo","_type":"house"}}
{"id":"1005","title":"整租 · 南京西路品质小区 21213三轨交汇 配套齐* 拎包入 住","price":"6000"}
{"index":{"_index":"mydemo","_type":"house"}}
{"id":"1006","title":"祥康里 简约风格 *南户型 拎包入住 看房随时","price":"7000"}

~~~



###  一、REST低级客户端

pom依赖

~~~xml
<dependencies>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-client</artifactId>
            <version>6.5.4</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.9.4</version>
        </dependency>
    </dependencies>

<build>
        <plugins>
<!-- java编译插件 --> <plugin>
          <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.2</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
~~~



测试用例

~~~java
package cn.tekin.es.restclient;

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
public class RestDemo {
    private static final ObjectMapper objMapper = new ObjectMapper();
//    private static final ObjectMapper MAPPER = new ObjectMapper();
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

~~~

从使用中，可以看出，基本和我们使用RESTful api使用几乎是一致的。



### 二、REST高级客户端

引入依赖

~~~xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>6.5.4</version>
</dependency>
~~~

测试用例

~~~java
package cn.tekin.es.restclient;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * REST高级客户端 测试
 * @author tekintian@gmail.com
 * @version v0.0.1
 * @since v0.0.1 2022-10-15 17:54
 */
public class TestRestHighLevel {
    private RestHighLevelClient client;

    @Before
    public void init() {
//        RestClientBuilder restClientBuilder = RestClient.builder(
//                new HttpHost("192.168.2.8", 9200, "http"),
//                new HttpHost("192.168.2.8", 9201, "http"),
//                new HttpHost("192.168.2.8", 9202, "http"));
        RestClientBuilder restClientBuilder = RestClient.builder(
                new HttpHost("192.168.2.8", 9200, "http"));
        this.client = new RestHighLevelClient(restClientBuilder);
    }

    @After
    public void after() throws Exception {
        this.client.close();
    }

    /**
     * 新增文档，同步操作
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", "2002");
        data.put("title", "南京西路 拎包入住 一室一厅");
        data.put("price", "4500");
        IndexRequest indexRequest = new IndexRequest("mydemo", "house")
                .source(data);
        IndexResponse indexResponse = this.client.index(indexRequest,
                RequestOptions.DEFAULT);
        System.out.println("id->" + indexResponse.getId());
        System.out.println("index->" + indexResponse.getIndex());
        System.out.println("type->" + indexResponse.getType());
        System.out.println("version->" + indexResponse.getVersion());
        System.out.println("result->" + indexResponse.getResult());
        System.out.println("shardInfo->" + indexResponse.getShardInfo());
    }

    /**
     * 新增文档，异步操作 *
     *
     * @throws Exception
     */
    @Test
    public void testCreateAsync() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", "2003");
        data.put("title", "南京东路 最新房源 二室一厅");
        data.put("price", "5500");
        IndexRequest indexRequest = new IndexRequest("mydemo", "house")
                .source(data);
        this.client.indexAsync(indexRequest, RequestOptions.DEFAULT, new
                ActionListener<IndexResponse>() {
                    @Override
                    public void onResponse(IndexResponse indexResponse) {
                        System.out.println("id->" + indexResponse.getId());
                        System.out.println("index->" + indexResponse.getIndex());
                        System.out.println("type->" + indexResponse.getType());
                        System.out.println("version->" + indexResponse.getVersion());
                        System.out.println("result->" + indexResponse.getResult());
                        System.out.println("shardInfo->" + indexResponse.getShardInfo());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.out.println(e);
                    }
                });
        Thread.sleep(20000);
    }

    @Test
    public void testQuery() throws Exception {
        GetRequest getRequest = new GetRequest("mydemo", "house",
                "202210");
// 指定返回的字段
        String[] includes = new String[]{"title", "id"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);
        GetResponse response = this.client.get(getRequest,
                RequestOptions.DEFAULT);
        System.out.println("数据 -> " + response.getSource());
    }

    /**
     * 判断是否存在
     *
     * @throws Exception
     */
    @Test
    public void testExists() throws Exception {
        GetRequest getRequest = new GetRequest("mydemo", "house",
                "GkpdE2gBCKv8opxuOj12"); // 不返回的字段
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        boolean exists = this.client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println("exists -> " + exists);
    }

    /**
     * 删除数据
     *
     * @throws Exception
     */
    @Test
    public void testDelete() throws Exception {
        DeleteRequest deleteRequest = new DeleteRequest("mydemo", "house",
                "GkpdE2gBCKv8opxuOj12");
        DeleteResponse response = this.client.delete(deleteRequest,
                RequestOptions.DEFAULT);
        System.out.println(response.status());// OK or NOT_FOUND
    }

    /**
     * 更新数据
     *
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest("mydemo", "house",
                "j0gJ24MBS0VRu4AUgcIY");
        Map<String, Object> data = new HashMap<>();
        data.put("title", "张江高科2");
        data.put("price", "5000");
        updateRequest.doc(data);
        UpdateResponse response = this.client.update(updateRequest,
                RequestOptions.DEFAULT);
        System.out.println("version -> " + response.getVersion());
    }

    /**
     * 测试搜索
     *
     * @throws Exception
     */
    @Test
    public void testSearch() throws Exception {
        SearchRequest searchRequest = new SearchRequest("mydemo");
        searchRequest.types("house");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("title", "拎包入住"));
        sourceBuilder.from(0);
        sourceBuilder.size(5);

        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(sourceBuilder);
        SearchResponse search = this.client.search(searchRequest,
                RequestOptions.DEFAULT);
        System.out.println("搜索到 " + search.getHits().getTotalHits() + " 条数据.");
        SearchHits hits = search.getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
~~~



## 三、Spring Data Elasticsearch

Spring Data项目对Elasticsearch做了支持，其目的就是简化对Elasticsearch的操作。

地址:https://spring.io/projects/spring-data-elasticsearch

ES版本和SpringBoot版本对照表

https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#preface.versions



这里采用SpringBoot整合的方式进行。

 注意这里的springboot版本为 2.2.x  

 SpringBoot Data Elasticsearch 的模式必须要与服务端版本对应，否则无法链接的 ， 

### pom.xml 依赖 

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.13.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>cn.tekin</groupId>
    <artifactId>es</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>es</name>
    <description>ES Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
<!-- ES rest-client 依赖 -->
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-client</artifactId>
            <version>6.5.4</version>
        </dependency>

<!-- REST高级客户端 -->
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>6.5.4</version>
<!-- 如果上面同时导入了 elasticsearch-rest-client 这里需要排除，因为2个包有同名的类，否则运行时会报错-->
            <exclusions>
                <exclusion>
                    <groupId>org.elasticsearch.client</groupId>
                    <artifactId>elasticsearch-rest-client</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <!-- Spring Data Elasticsearch 依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        </dependency>

<!-- SpringBoot 其他依赖 -->

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.9.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.4</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

~~~



### application.yml

~~~properties
spring:
  application: 
    name: elasticsearch-client-demo
  data:
    elasticsearch: # 这个配置在ES7中已经丢弃
      cluster-name: "docker-cluster"
      cluster-nodes: 192.168.2.8:9300 # 多个以逗号分隔即可
~~~

这里要注意，使用的端口是9300，而并非9200，原因是9200是RESTful端口，9300是API端口。

### SpringBoot启动类

~~~java
package cn.tekin.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsApplication.class, args);
    }
}
~~~

### 编写测试用例 

- 编写User对象

~~~java
package cn.tekin.es.pojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "ynws", type = "user", shards = 6, replicas = 1)
public class User {
@Id
    private Long id;
    @Field(store = true)
    private String name;
    @Field
    private Integer age;
    @Field
    private String hobby;
}
~~~

- 新增数据

~~~java
package cn.tekin.es.client;

import cn.tekin.es.pojo.User;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestSpringDataES {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 新增数据测试
     */
    @Test
    public void testSave() {
        User user = new User();
        user.setId(1001L);
        user.setAge(20);
        user.setName("张三");
        user.setHobby("足球、篮球、听音乐");
        IndexQuery indexQuery =
                new IndexQueryBuilder().withObject(user).build();
        String index = this.elasticsearchTemplate.index(indexQuery);
        System.out.println(index);
    }
	/// 其他方法见下面....
}
~~~

- 批量插入数据

~~~java
/**
     * 批量插入数据
     */
    @Test
    public void testBulk() {
        List list = new ArrayList();
        for (int i = 0; i < 5000; i++) {
            User user = new User();
            user.setId(1001L + i);
            user.setAge(i % 50 + 10);
            user.setName("张三" + i);
            user.setHobby("足球、篮球、听音乐");
            IndexQuery indexQuery = new
                    IndexQueryBuilder().withObject(user).build();
            list.add(indexQuery);
        }
        Long start = System.currentTimeMillis();
        this.elasticsearchTemplate.bulkIndex(list);
        System.out.println("用时:" + (System.currentTimeMillis() - start)); //用时:
    }

~~~

- 局部更新，全部更新使用index覆盖即可

~~~java
/**
     * 局部更新，全部更新使用index覆盖即可
     */
    @Test
    public void testUpdate() {
        testSave(); //先新增这个数据
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.source("age", "30");
        UpdateQuery updateQuery = new UpdateQueryBuilder()
                .withId("1001")
                .withClass(User.class)
                .withIndexRequest(indexRequest).build();
        this.elasticsearchTemplate.update(updateQuery);
    }
~~~

- 删除数据

~~~java
@Test
public void testDelete(){
    this.elasticsearchTemplate.delete(User.class, "1001");
}
~~~

- 搜索

~~~java
/**
* 搜索
 */
@Test
public void testSearch() {
  PageRequest pageRequest = PageRequest.of(1, 10); //设置分页参数
  SearchQuery searchQuery = new NativeSearchQueryBuilder()
    .withQuery(QueryBuilders.matchQuery("name", "张三")) // match查询
    .withPageable(pageRequest)
    .build();
  AggregatedPage<User> users =
    this.elasticsearchTemplate.queryForPage(searchQuery,
                                            User.class);
  System.out.println("总页数:" + users.getTotalPages()); //获取总页数
  for (User user : users.getContent()) { // 获取搜索到的数据
    System.out.println(user);
  }
}
~~~

