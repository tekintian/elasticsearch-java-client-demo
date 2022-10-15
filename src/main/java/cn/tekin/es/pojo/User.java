package cn.tekin.es.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

/**
 * @author tekintian@gmail.com
 * @version v0.0.1
 * @since v0.0.1 2022-10-15 18:42
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "ynws", type = "user", shards = 2, replicas = 0)
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