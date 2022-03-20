### Introduction

최근에 어떤 애플리케이션을 만들면서 겪은 일입니다. 조직이나 그룹별로 서로 다른 Database 에 접근해야 했습니다. 걔다가 그 조직은 동적으로 증가할 수 있었기 때문에 Predefine 할 수 없었습니다. 결국 사용자별 DB 요청 시에 Datasource 연결을 달리 해야 하는 이슈가 있었는데 다행도 스프링에서는 위 문제를 해결할 수 있는 추상 클래스를 제공하고 있었습니다. 이 글에서는 간단한 코드로 Spring에서 런타임 시 동적으로 Datasource를 결정하는 방법을 알아보겠습니다.

### 구성

#### build.gradle

최신 버전의 spring boot 와 mybatis, mysql connector 등의 dependency 포함하고 있는 간단한 build.gradle 파일입니다.

```gradle
plugins {
    id 'org.springframework.boot' version '2.1.8.RELEASE'
    id 'io.spring.dependency-management' version '1.0.8.RELEASE'
    id 'java'
}

group = 'info.m2sj'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '1.8'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation "org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.0"
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    compile 'mysql:mysql-connector-java:8.0.17'
}

```

#### Database

테스트할 데이터베이스는 MySQL로 하였습니다. 물론 어느 DB도 상관없습니다. 하지만 최소 2개 이상의 DB가 있어야지 테스트 해볼수 있겠죠 여기서는 같은 IP, Port를 같는 2개의 Database를 만들어서 테스트했습니다. 각각의 DB에 테이블을 생성하고 해당 테이블에 데이터를 입력합니다.

```sql
create table route_test
(
	db_name varchar(255) null
);

insert into route_test value ('this is db_01');
insert into route_test value ('this is db_02');
```

#### MyRoutingDataSource

Spring에서 제공하는 AbstractRoutingDataSource 클래스를 상속받은 클래스입니다. AbstractRoutingDataSource 에는 determineCurrentLookupKey라는 추상 메서드가 존재하는데 이름에서 알 수 있듯이 현재 요청의 연결할 Datasource를 결정할 Key 값을 리턴합니다. MyRoutingDataSource 구현하고 있는 determineCurrentLookupKey 메서드를 살펴보면 로그인 세션의 정보를 조회해서 선택한 DB Key를 넘겨주고 있습니다.

```java
package info.m2sj.springrouterdatasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class MyRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        Object dbKey = RequestContextHolder
        	.getRequestAttributes()
        	.getAttribute("db_key", RequestAttributes.SCOPE_SESSION);
            
        return "current:" + dbKey;
    }
}

```

#### DatabaseConfig

Datasource Bean 을 생성하는 Configuration 클래스입니다. 일반적인 Datasource가 아닌 MyRoutingDataSource 를 생성해서 리턴하고있습니다. 결국 MyRoutingDataSource 여러개의 Datasource 객체를 Key, Value 형태로 담고 있고 determineCurrentLookupKey라는 메소드에서 리턴하는 Key 값과 매칭되는 Datasource 객체를 반환하게됩니다.

```java
package info.m2sj.springrouterdatasource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource createRouterDatasource() {
        AbstractRoutingDataSource routingDataSource = new MyRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();

        targetDataSources.put("current:db01", 
        	createDataSource("jdbc:mysql://localhost:3306/test_db02", "test", "1234"));

        targetDataSources.put("current:db02", 
        	createDataSource("jdbc:mysql://localhost:3306/test_db01","test", "1234"));


        routingDataSource.setTargetDataSources(targetDataSources);

        return routingDataSource;
    }

    private DataSource createDataSource(String url, String user, String password) {
        com.zaxxer.hikari.HikariDataSource dataSource = 
        	new com.zaxxer.hikari.HikariDataSource();
            
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);
        return dataSource;
    }
}

```

#### MyMapper

annotation 을 이용한 간단한 mybatis mapper 클래스입니다.

```java
package info.m2sj.springrouterdatasource;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MyMapper {
    @Select("select db_name from route_test")
    String findDbName();
}

```

#### SpringrouterdatasourceApplication

Spring Boot run 클래스입니다. 코드를 간단히 하기 위해서 Controller를 포함하고 있습니다. 각각 URI 맵핑 메소드 안에서  
서로 다른 DB Key를 세션에 담고 있습니다.

```java
package info.m2sj.springrouterdatasource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@SpringBootApplication
public class SpringrouterdatasourceApplication {
    private MyMapper myMapper;

    public SpringrouterdatasourceApplication(MyMapper myMapper) {
        this.myMapper = myMapper;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringrouterdatasourceApplication.class, args);
    }

    @RestController
    @RequestMapping("/route")
    class TestController {

        @GetMapping("/a")
        public String getA(HttpSession session) {
            session.setAttribute("db_key","db01");
            return myMapper.findDbName();
        }

        @GetMapping("/b")
        public String getB(HttpSession session) {
            session.setAttribute("db_key","db02");
            return myMapper.findDbName();
        }
    }
}

```

### 테스트

각 요청에 결과를 확인하면 서로 다른 Database 에 연결되고 있는 것을 확인 할 수 있습니다.  

```
http://localhost:8080/route/a => "this is db_01"

http://localhost:8080/route/b => "this is db_02"
```

### Conclusion

스프링에서 DB 요청을 할때마다 동적으로 Datasource 를 결정하는 방법을 알아 봤습니다. 물론 코드에서 많은 부분이 생략돼 있지만 AbstractRoutingDataSource 를 구현하는 커스텀 클래스를 여러 형태로 응용해서 여러 케이스의 기술적 요구 사항 들을 해결할 수 있습니다.
