# Spring-DB02

## 2023-03-14
### SQL Mapper vs ORM 기능
- SQL Mapper는 Spring-DB01 에서 잠깐 봤듯이, 개발자가 SQL만 입력을 하고 그 외에 중복되던 기능들(conn, pstmt 등등)은 제거해주고 편리한 기능을 제공해준다.
- ORM은 설명해야 될 부분은 많지만 간단하게 설명하자면 JPA를 사용하면 기본적인 SQL은 JPA가 대신 처리해주는데 JPA는 그저 자바 진영의 ORM(Object Relational Mapping)의 표준이고, JPA를 구현하는 구현체인 hibernate를 주로 사용한다.   
단순히 Spring Data JPA나, queryDSL은 이러한 JPA 사용을 좀 더 편리하게 사용할 수 있게 해주는 도구이다.
