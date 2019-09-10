Spring Dynamic Routing Database

## Introduction
최근에 어떤 어플리케이션을 만들면서 겪은 일입니다.
조직이나 그룹별로 서로 다른 Database 에 접근해야 했습니다. 걔다가 그 조직이 A, B, C 이렇게 Predefine 할 수 있는게
아니라 동적으로 증가할 수 있었습니다. 결국 사용자별 DB 요청시에 Datasource 연결을 달리 해야하는 이슈가 있었는데 다행이도 스프링에서는
위 문제를 해결할수 있는 추상 클래스를 제공하고 있었습니다. 이 글에서는 간단한 코드로 Spring 에서 런타임시 동적으로 Datasource 를 결정하는 방법을 알아 보겠습니다.


## 구성

### build.gradle


### 데이터베이스
테스트할 데이터베이스는 MySQL 로 하였습니다. 물론 어느 DB도 상관 없습니다. 하지만 최소 2개이상의 DB가 있어야지 테스트 해볼수 있겠죠
여기서는 같은 IP,Port 에 2개의 Database 를 만들어서 테스트 했습니다. 각 DB에 테이블을 생성하고 해당 테이블에 데이터를 입력합니다.
create table route_test
(
	db_name varchar(255) null
);

insert into route_test value ('this is db_01');
insert into route_test value ('this is db_02');

### MyRoutingDataSource
Spring 에서 제공하는 AbstractRoutingDataSource 클래스를 상속받은 클래스입니다. AbstractRoutingDataSource 에는  determineCurrentLookupKey라는 추상 메소드가 
존재하는데 이름에서 알수 있듯이 현재 요청의 연결할 Datasource 를 결정할 Key 값을 리턴합니다. 해당 메소드를 MyRoutingDataSource 구현하고 있습니다. 
이 글에서는 로그인 세션의 정보를 조회해서 선택한 DB Key 를 넘겨주고 있습니다.


### DatabaseConfig
Datasource Bean 을 생성하는 Configuration 클래스입니다. 일반적인 Datasource가 아닌 MyRoutingDataSource 를 생성해서 리턴하고있습니다.
결국 MyRoutingDataSource 여러개의 Datasource 객체를 Key, Value 형태로 담고 있고 determineCurrentLookupKey라는 메소드에서 리턴하는 Key 값과 매칭되는
Datasource 객체를 반환하게됩니다.

### MyMapper
annotation 을 이용한 간단한 mybatis mapper 클래스입니다. 


### SpringrouterdatasourceApplication
Spring Boot run 클래스입니다. 소스를 간단히 하기 위해서 Controller 를 포함하고 있습니다. 각각 URI 맵핑 메소드의 세션에서
서로 다른 DB Key를 담고 있습니다.


## 테스트
각 요청에 결과를 확인하면 서로 다른 Database 에 연결되고 있는 것을 확인 할 수 있습니다.
http://localhost:8080/route/a
http://localhost:8080/route/b

## Conclusion
스프링에서 DB 요청을 할때마다 동적으로 Datasource 를 결정하는 방법을 알아 봤습니다. 물론 코드에서 많은 부분이 생략돼 있지만 AbstractRoutingDataSource
를 구현하는 커스텀 클래스를 여러 형태로 응용해서 여러 케이스의 기술적 요구 사항 들을 해결할 수 있습니다. 
