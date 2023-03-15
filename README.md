# Spring-DB02

# 2023-03-14
### SQL Mapper vs ORM 기능
- SQL Mapper는 Spring-DB01 에서 잠깐 봤듯이, 개발자가 SQL만 입력을 하고 그 외에 중복되던 기능들(conn, pstmt 등등)은 제거해주고 편리한 기능을 제공해준다.
- ORM은 설명해야 될 부분은 많지만 간단하게 설명하자면 JPA를 사용하면 기본적인 SQL은 JPA가 대신 처리해주는데 JPA는 그저 자바 진영의 ORM(Object Relational Mapping)의 표준이고, JPA를 구현하는 구현체인 hibernate를 주로 사용한다.   
단순히 Spring Data JPA나, queryDSL은 이러한 JPA 사용을 좀 더 편리하게 사용할 수 있게 해주는 도구이다.

### DTO에 대해서
- DTO가 데이터 전달 객체 인 건 알고 있었지만, 자세히 알고 사용하진 않았다. 고민해보지 않은 영역인 DTO 에서 기능을 사용해도 되냐에 대한 얘기가 나왔는데, 꼭 필요하다면 어느 정도의 기능이 있어도 된다고 한다. 예를 들어서 들어온 데이터에 대해서 포맷팅 해줘야하는 경우에 대해서 말이다. 또한 해당 객체의 주 목적이 데이터를 전달하기 위함이 목적이라면 해당 객체는 DTO가 될 수 있다.   
- 여기서 데이터라 하면 꼭 주고 받는 데이터가 아닌, 우리가 동적 검색을 하는 경우에도 데이터를 전달하는 것이기 때문에 DTO라고 할 수 있는 것이다. 추가적으로, DTO를 어느 패키지에 두어야 하는지에 대한 얘기도 나왔다. 프로젝트를 진행하면서 많이 고민했었던 부분인데, 어디에서 호출이 마지막으로 이루어지는지에 따라서 다르게 두면 된다고 한다. 예를 들어서, Controller에서만 쓰고 버린다면 Controller에, Service에서 어떠한 DTO의 변환을 위해서 사용해야 한다면 Service에 동적 검색 등을 위해서 마지막으로 Repository를 참조해야한다면 Repository에 두는 것이 맞다고 한다. 물론 이런 기준을 두지 않고 DTO 란 패키지를 만들어서 사용해도 된다고 하는데 아마 접근 제어자의 범위에서 해당 패키지에서만 사용하는 것을 목적으로 말씀을 해주신 것 같다. 다음 프로젝트 리팩토링 때 해당 사항을 적용해보도록 해야겠다. 

### EventListener와 PostConstruct
- 기존에 프로젝트에서 사용했던 애노테이션들이다. PostConstruct보다 EventListener를 사용하는 것이 테스트케이스 호출 시 더 좋다는 말을 검색해서 찾아보고 사용했었는데 그것에 대한 이유를 알 수 있었다. EventListener의 프로퍼티로 ApplicationReadyEvent를 사용하면 해당 애노테이션은 스프링의 애노테이션이라서 스프링 컨테이너가 초기화가 된 이후 즉, Bean 들이 전부 등록 된 이후에 실행되는 애노테이션이다.   
- 그러나 PostConstruct는 자바 표준의 애노테이션인데 PostConstruct의 수행 시점은 객체가 생성된 이후에 발생한다. 이 말은 즉, 스프링 컨테이너가 전부 초기화 되기 전에 해당 객체가 생성되면서 실행된다는 것인데, 이렇게 되면 @Transactional 같은 AOP가 적용되지 않은 상태로 실행이 될 수 있는 문제가 있다는 것이다. AOP 또한 스프링이 관리를 하고 등록을 해줘야하는 부분인데 그런 영역이 초기화 되기 전에 실행될 수 있다는 문제가 있다는 것이다. 둘의 차이점에 대해서 자세히 알고 사용해야 할 것 같다.

### AfterEach 에서의 다형성 적용
- 기존 프로젝트에서 로컬 메모리 테스트와 DB 테스트를 진행하면서 로컬은 테스트를 진행하는데 있어서, 메모리가 초기화되어야하는 반면 데이터베이스는 초기화를 하면 안됐었다. 따라서, 두 개의 branch를 따로 구분해서 실행이 되도록 하여 DB를 사용하는 branch에서는 @AfterEach를 주석처리 함으로써 해결했었는데, 이 당시에는 다형성에 대한 이해도가 부족했기 때문에 발생한 문제라고 생각한다.   
- instanceof 를 활용하여 객체의 형변환이 가능한지 체크하고 만약에 들어온 구현체가 MemoryRepository 라면 해당 구현체만 실행이 되게 함으로써 유연하게 구성을 할 수 있었다. DB 같은 경우에는 테스트 이후에 Transaction으로 Rollback 처리를 하면 되기때문에 @AfterEach 를 굳이 사용 할 필요가 없다. 스프링은 객체지향 개발을 좀 더 원할하게 할 수 있도록 도와주는 프레임워크이다. 이 부분에서도 객체지향을 제대로 이해하는 것이 중요하다는 것을 알았다.

## JdbcTemplate
- JdbcTemplate는 SQL Mapper 기능을 제공해주는 것중에 하나로, 위에 작성되어있듯이 여러가지 반복 작업을 제거해준다. 커넥션 획득, Statement 준비와 실행, release, 예외 발생 시 스프링 예외 변환기 실행 까지 다양한 부분을 도와준다. 굉장히 좋은 도구이지만 아쉽게도 동적 쿼리에 대한 적용이 어렵다.

### JdbcTemplate 데이터 변경
- JdbcTemplate 에서 데이터를 변경하는 부분에서는 `template.update()` 를 사용해야 한다. 데이터를 변경한다고 하면 INSERT, UPDATE, DELETE를 얘기한다. 구시대적인 방법으로 autoIncrement로 기본키가 자동으로 올라가면서 Item을 응답할 때 값을 넘기기 위해 Id를 받아와야하는데 이때, KeyHolder를 사용해서 가져올 수 있다.

### JdbcTemplate 데이터 조회
- 데이터를 조회하는 경우에는 두 가지가 있다. template.queryForObject(), template.query() 이다. 전자의 경우 한 개의 결과값만을 반환하고, 후자의 경우 여러개를 반환한다. 예제 코드에서는 Optional을 사용하여 null 방지를 해두었는데, JdbcTemplate는 스프링 예외 변환기도 자동으로 해주기 때문에, 결과가 없으면 EmptyResultDataAccessException 을 반환한다. 전자의 경우 데이터가 여러개가 호출되어도 예외가 발생하는데 이때는 IncorrectResultSizeDataAccessException 예외가 발생한다.
- 데이터 조회를 위해서 RowMapper를 사용한 것을 볼 수 있는데 RowMapper는 기존의 JdbcTemplate를 안쓰던 시절에 사용하던 `while(!rs.next())`내부 로직을 대신 실행해주는 것이다.

# 2023-03-15
### JdbcTemplate 이름 지정 파라미터
- 기존에 `JdbcTemplate` 에서 파라미터를 바인딩 할 때에 꼭 순서를 맞춰서 바인딩을 해주어야 했다. 연습을 하는 과정에서는 파라미터의 개수가 적기도 하고, 나 혼자 하는 것이기 때문에 문제가 발생할 일이 없지만 실제로 협업을 하면서 다른 개발자가 실수로 위치를 바꾼다거나, 새로운 파라미터가 추가가 된다거나, 이게 찾기 쉬울 것 같지만 실무에서는 파라미터 개수가 많게는 20개도 넘기 때문에 찾는 것도 어렵고, 만약에 데이터가 잘못 들어가게 되었을 때, 최근에는 데이터를 시간 단위로 백업을 하면서 롤백을 가능하게 하는 기능이 있으나, 그렇게 하면 전체 데이터가 롤백이 되는 것이기때문에, 특정 데이터만 롤백이 되도록 해야한다. 그렇게 잘못 들어간 데이터들을 고쳐야 하는데, 쉬운 작업은 아니다.
- 이러한 문제를 해결하기 위해서 이름 지정 파라미터가 등장했고, 말 그대로 순서가 아닌 우리가 평소에 Map을 Key Value를 사용했던 것처럼 key에 따라 값을 넣는 방식이다. JdbcTemplate를 사용하지 않고 `NamedParameterJdbcTemplate` 를 사용한다. 실제로 보면 사용하는 쿼리들은 비슷해서 인터페이스를 하고 다형성을 이용하는게 좋을 것 같은데 당시에는 인터페이스를 효율적으로 사용하지 않았던 탓일까 인터페이스로 구현이 되어있지도 않았고, JdbcTemplate를 실제로도 많이 사용하지 않기 때문에 인터페이스로 교체를 안하는 것 같다.
- `NamedParameterJdbcTemplate`는 이제 `이름 지정 파라미터`를 사용할 수 있는데, 파라미터 지정방식이 크게 3가지가 있다.
  1. `SqlParameterSource` 인터페이스 사용 : SqlParamterSource 인터페이스는 `BeanPropertySqlParameterSource`, `MapSqlParameterSource` 라는 두 개의 구현체를 사용한다.
    - 먼저 `BeanPropertySqlParameterSource`는 객체를 가지고 오면 해당 객체에 있는 내용과 sql에 이름을 지정한 매핑을 보고 알아서 매핑해주는 아주 유용한 도구이다. 실제로도 `MapSqlParameterSource` 보다 더 편리한 기능을 제공하는데, 단점은 객체에 없는 다른 파라미터를 추가할 수 없다는 것이 문제이다. 따라서 이럴 경우에 사용하는게 `MapSqlParameterSource` 이다.
    - `MapSqlParameterSource` 는 메소드 체이닝 방식을 이용해서 `.addValue(key, value)` 를 넣어주는 방식이다. `BeanPropertySqlParameterSource`와 달리 이름을 지정한 매핑에 직접 값을 넣어주는 것이기 때문에 확장성이 좋지만, 귀찮을 수 있다는 문제가 있다.
  2. `Collections Map`의 순수 기능을 사용하는 것이다. Map을 생성하고 거기에 key, value를 통해 값을 지정하면 된다. Map의 순수기능을 편리하게 해주는 것이 `MapSqlParameterSource` 라고 생각하면 된다.
- 그리고 RowMapper 관련해서도 더욱 개선이 되었다. 기존에 `RowMapper`에 있는 필드들을 다 적고 직접 매핑해줘야 하는 문제가 있었지만, `BeanPropertyRowMapper` 라는 스프링에서 제공해주는 클래스를 사용하면 `BeanPropertyRowMapper.newInstance(xxx.class)` 메서드를 사용하여 Property 규약을 통해 알아서 매핑해준다. Property 규약을 통해 매핑해주는 것이기 때문에 Getter, Setter 가 꼭 있어야 한다. 그러나 DB에 작성 규약은 snake 방식이고 java는 camel 방식이라서 옛날에는 db를 조회할 때 필드이름 as java필드이름 으로 별칭을 지정해줘야 했다. 예를 들어서 item_name as itemName 처럼 말이다. 이렇게 규약이 다르기 때문에 귀찮은 작업이 있었는데 현재에는 알아서 camel 케이스로 변환을 해주어서 크게 신경 쓸 필요가 없다고 한다. 

### SimpleJdbcInsert
- Insert를 편리하게 만들어주는 클래스이다. 다음과 같이 사용할 수 있다. SimpleJdbcInsert를 물론 빈으로 등록해서 다른 곳에서도 사용 할 수 있지만 보통 Create는 한 곳에서 사용하는 것도 많고 빈으로 등록하게 될 때 어차피 TableName도 다 지정을 해줘야 하기 때문에, 빈으로 등록하는 것을 권장하지는 않는다. 
```java
new SimpleJdbcInsert(dataSource)
  .withTableName("item")
  .usingGeneratedKeyColumns("id") // 기본키 자동생성 PK 이름
  .usingColumns("item_name", "price", "quantity"); // 생략 가능
```
- 실제로 save를 하게 될 때 필요한 파라미터는 이전에 배운 `BeanPropertySqlParameterSource`를 이용해서 parameter를 만들고 `jdbcInsert.execute(param)`을 하게 되면 바로 생성이 된다. 기본키가 필요할 경우에는 `jdbcInsert.executeAndReturnKey(param)` 을 사용해서 기본키를 받으면 된다.

## Database Test

### 테스트 데이터 베이스 분리
- 기존 프로젝트를 하면서 개발 용 데이터 베이스와 테스트를 하기 위한 데이터 베이스를 서로 공유하면서 사용하였다. 어차피 개발용 데이터 베이스는 실행 할 때마다 초기화 될 것이고, 크게 신경쓰지 않아도 되지 않을까? 라는 판단하에 진행했었는데, 테스트의 원칙에는 다음과 같은 중요한 원칙이 있다고 한다.
1. 테스트는 다른 테스트와 격리해야 한다.
2. 테스트는 반복해서 실행할 수 있어야 한다.   

이런 원칙에 입각하여, 테스트를 할 때에는 다른 데이터에 영향을 끼치면 안된다. 즉, 내가 했던 방법은 다른 케이스에도 영향을 끼칠 수 있는 상태이기 때문에 문제가 발생하는 것이다. 따라서 테스트를 진행할 때에는 테스트를 진행하기 위한 새로운 테스트 전용 데이터베이스를 만드는 것이 좋다고 한다.

### 테스트 종료 후 테스트 데이터 정리
- 해당 부분은 바로 트랜잭션을 이용하면 해결할 수 있을 것이라고 생각 했다. 기존에 Connection을 유지하면서 실행하는 방식이 아닌, 이전에 배운 트랜잭션 동기화 매니저를 이용하여 테스트 실행 전에 트랙잭션을 시작하고 테스트 종료 이후에 rollback을 진행하여 테스트 케이스를 깔끔하게 정리할 수 있었다. PlatformTransactionManager 인터페이스를 이용하여 Transaction을 열고 그 status를 받아서 롤백을 진행할 수 있었다. 하지만 우리가 이전에 배운 @Transactional 애노테이션을 이용하면 AOP를 통해 이러한 과정을 더 간단하게 수행하게 해준다.   
- 근데 여기서 의문이 생길 것이다. @Transactional 애노테이션은 기존에 서비스 로직에서 사용할 때 서비스가 성공적으로 동작하면 Commit을 해주고 중간에 실패를 해야만 Rollback를 해주는 기능을 가지고 있었는데 왜 사용하는 거지? 테스트에서는 왜 롤백이 되는 것일까? 라는 의문말이다. 이유는 스프링이 테스트에서 @Transactional 애노테이션을 사용하면 위에서 얘기했던 테스트의 두 가지 특징이 지켜져야 하기 때문에 트랜잭션이 성공적으로 마무리 되면 rollback이 되도록 하고 있기 때문이다.   
- 추가적으로 때로는 테스트 케이스가 잘 통과하더라도 데이터가 제대로 들어갔는지 확인하고 싶은 경우가 있다. 그럴 경우에는 메서드에 @Commit 애노테이션을 추가해주면 정상적으로 커밋이 된다. 항상 애노테이션에서의 기준은 넓은 범위에서 좁은 범위로 들어갈수록 좁은 범위가 더 높은 우선순위를 가지고 있기 때문이다.

### 임베디드 모드 DB
- 테스트를 수행할 때, 위에서 처럼 별도의 데이터베이스를 분리하고, 테스트를 하는 작업은 번거로운 일이다. H2 데이터베이스는 자바로 개발이 되어있는데 특수하게 JVM 안에서 메모리 모드로 동작이 가능하다. 이렇게 애플리케이션에 내장되서 사용한다고 해서 임베디드 모드라고 한다. resources 패키지 아래에 schema.sql 파일은 만들어서, 테이블 생성 쿼리를 등록을 해놓으면 애플리케이션이 올라가는 단계에서 자동적으로 테이블이 생성이 된다. [참고 공식문서](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.using-basic-sql-scripts)
- 추가적으로 이전에 test에서 프로필을 나누어 사용하면서 기존에 개발모드에서 쓰는 db를 가져다가 똑같은 링크를 두고 사용했었는데, 아무것도 입력해주지 않으면 스프링부트가 데이터소스를 등록할 때 url을 자동으로 고유한 메모리로 처리를 해준다고 한다. 예전에 테스트 당시에 저런 문제 때문에 충돌 문제가 있었는데, 서로 다른 db 영역을 사용하기 때문에 충돌이 발생할 문제 또한 없다. 또한 고유한 db이름을 사용하지 않도록 할 수 있는데, 지정하면 testdb 라는 이름으로 등록이 된다. 그러나 testdb가 또 다르게 필요한 것이 여러 개가 있을 수도 있기 때문에 권장하지는 않는다. 

## MyBatis

### MyBatis 사용 이유
기존에 JdbcTemplate를 사용했을 때 SQL을 개발자가 직접 작성해줄 때, 다음과 같은 두 가지 문제점이 있었다.
1. 동적 쿼리 작성에 어려움
2. SQL을 직접 작성하면서 긴 SQL을 작성할 때 실수로 띄어쓰기를 누락했을 경우   

2번 예시는 다음과 같은 상황을 얘기하는 것이다.
```java
String sql = "select image_name, image_original_name, image_create_date, image_update_date"
      + "from image";
      // select image_name, image_original_name, image_create_date, image_update_datefrom image <-- 오류 발생
```
MyBatis는 Xml을 이용함으로써 위와 같이 띄어쓰기를 크게 신경써주지 않아도 되고, <where> </where> <if> </if> 같은 문법이 있어 동적 쿼리를 작성하는데에도 편리함을 가져다준다. 참고로 이름 지정 파라미터를 사용한다.   
기존에 jdbcTemplate에서도 snakeCase를 camelCase로 변경해줬던 것 처럼 myBatis도 동일하게 해당 기능을 제공해준다.
단 Mapper를 지정할 때 타입 정보를 입력해줘야 하는데 properties, yml에서 type-aliases-package에 패키지 명을 지정해주면 타입 정보를 입력할 때 패키저 이름을 생략하고 타입 정보 지정이 가능하다. 여러 위치는 `,` , `;` 로 구분 가능하다.

### MyBatis Mapper 빈 등록
- MyBatis에서 쿼리를 구현하기 위한 인터페이스를 설정해두고 `@Mapper` 라는 애노테이션을 사용하고 구현체를 생성하지 않았음에도, 빈으로 등록할 수 있었다. 인터페이스 그 자체는 빈에 등록할 수 없는데 어떻게 가능했던 것일까? 그 이유는 `@Mapper` 라는 애노테이션을 사용하면 MyBatis 스프링 연동 모듈이 Mapper 구현체를 프록시 패턴으로 생성하고 빈으로 등록하는 것이였다. 따라서 실제로 스프링 빈으로 주입 받은 구현체의 `getClass()` 를 찍어보면 다음과 같이 나온다. `class com.sun.proxy.$Proxy66` $가 있다는 건 내부 클래스로 구현이 된다는 것인데, 이후에 배우게 될 것이 너무 기대된다.
