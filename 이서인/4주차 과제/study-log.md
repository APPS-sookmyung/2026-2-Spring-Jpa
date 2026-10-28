# 앱스 JPA 스터디 4주차 강의 내용 요약 (섹션 3~4)


이번 강의에서는 JPA를 이용한 데이터 조회 방법인 JPQL과 Criteria를 학습하고, Spring Data JPA를 활용하여 Repository를 구현하는 방법을 다루었다. 또한 다양한 검색 조건을 처리하는 Specification, 정렬 및 페이징 기능과 JPA의 기타 어노테이션을 학습하였다.

### 18. JPQL

JPQL(Java Persistence Query Language)은 SQL과 유사한 문법을 사용하지만, 데이터베이스 테이블이 아닌 엔티티와 엔티티의 속성을 대상으로 쿼리를 작성하는 언어이다.

- `SELECT`, `FROM`, `WHERE` 등을 이용하여 데이터를 조회할 수 있다.
- `EntityManager.createQuery()`를 이용하여 JPQL 쿼리를 생성한다.
- `WHERE`절에서 `AND`, `OR` 등을 활용하여 검색 조건을 조합할 수 있다.
- `setParameter()`를 통해 이름 기반 또는 위치 기반 파라미터를 전달할 수 있다.
- 복잡한 조인, DBMS에 특화된 기능 등이 필요한 경우 일반 SQL(Native Query) 사용을 고려할 수 있다.

### 19. Criteria

Criteria는 문자열로 쿼리를 작성하는 JPQL과 달리, 자바 코드로 쿼리를 동적으로 구성할 수 있는 API이다.

- `CriteriaBuilder`를 생성하고 `CriteriaQuery`를 통해 조회 결과 타입을 지정한다.
- `Root`를 사용하여 조회할 엔티티와 속성에 접근한다.
- `select()`, `where()`, `orderBy()`를 통해 조회 대상, 검색 조건, 정렬 순서를 지정한다.
- `Predicate`를 이용하여 검색 조건을 생성하고 `and()`, `or()`로 조건을 조합할 수 있다.
- `asc()`, `desc()`를 이용하여 오름차순과 내림차순 정렬을 설정한다.
- 검색 조건이 실행 상황에 따라 달라지는 동적 쿼리를 작성할 때 유용하다.
- 다만 복잡한 조회나 통계 처리에는 JPQL 또는 Native Query 등이 더 적합할 수 있다.

### 20. JPA 기타 특징

JPA와 Hibernate에서 제공하는 다양한 기능을 학습하였다.

- AttributeConverter: 자바 타입과 DB 타입 간의 변환을 처리한다. 예를 들어 `boolean` 타입을 DB의 `CHAR(1)` 타입으로 변환할 수 있다.
- @Formula: SQL 표현식이나 함수의 결과를 엔티티 속성에 매핑하는 Hibernate 기능이다. 주로 조회 시 계산된 값을 가져올 때 사용한다.
- @DynamicUpdate: 변경된 컬럼만 UPDATE 쿼리에 포함한다.
- @DynamicInsert: null인 속성을 INSERT 쿼리에서 제외하여 DB 기본값을 활용할 수 있다.
- @Immutable: 엔티티를 읽기 전용으로 취급하여 변경 감지 및 업데이트를 방지한다.
- @Subselect: SQL SELECT 결과를 읽기 전용 엔티티로 매핑하는 Hibernate 기능이며, 일반적으로 `@Immutable`과 함께 사용한다.

### 21. Spring Data JPA 시작하기

Spring Data JPA를 활용하면 Repository 인터페이스만으로 기본적인 데이터베이스 접근 기능을 구현할 수 있다.

- 엔티티별로 Repository 인터페이스를 생성한다.
- `JpaRepository` 등의 인터페이스를 상속하여 기본적인 CRUD 기능을 제공받는다.
- Spring Data JPA가 Repository 구현 객체를 자동으로 생성하고 스프링 빈으로 등록한다.
- 메서드 이름 규칙을 이용하여 별도의 SQL 작성 없이 조회 기능을 구현할 수 있다.

### 22. Repository 메서드 작성 규칙

Spring Data JPA는 정해진 메서드 이름 규칙에 따라 쿼리를 자동 생성한다.

- save(): 엔티티 저장 및 수정에 사용한다. 새 엔티티는 `persist()`, 기존 엔티티는 `merge()`를 이용한다.
- findById(): 식별자를 기준으로 엔티티를 조회하며, `JpaRepository`에서는 `Optional<T>`를 반환한다.
- delete(), deleteById(): 엔티티를 삭제한다.
- findBy속성이름(): 특정 속성값을 조건으로 데이터를 조회한다.
- findAll(): 전체 엔티티를 조회한다.

새 엔티티 여부는 `Persistable.isNew()`, 버전 속성, 식별자 값 등을 기준으로 판단한다. 메서드 이름을 활용한 조회는 간단한 조건에 적합하지만, 조건이 복잡해지면 메서드 이름이 길어지고 관리하기 어려워질 수 있다.

### 23. 정렬, 페이징, @Query

Spring Data JPA에서 조회 결과를 정렬하거나 페이지 단위로 나누는 방법을 학습하였다.

- 정렬: 메서드 이름에 `OrderBy`를 추가하거나 `Sort` 객체를 이용하여 정렬 조건을 지정한다.
- 페이징: `Pageable`, `PageRequest`를 이용하여 페이지 번호, 크기, 정렬 조건 등을 설정한다.
- Page 타입: 조회 데이터뿐 아니라 전체 데이터 수, 전체 페이지 수 등 페이징에 필요한 정보를 함께 제공한다.
- @Query: 메서드 이름 규칙 대신 JPQL을 직접 작성하여 원하는 조회 기능을 구현할 수 있다.

### 24. Specification

Specification은 Criteria API를 기반으로 검색 조건을 구성할 수 있도록 지원하는 인터페이스이다.

- 검색 조건을 별도의 Specification으로 정의하여 재사용할 수 있다.
- 람다식을 이용하여 조건을 간결하게 작성할 수 있다.
- `and()`, `or()`를 활용하여 여러 조건을 조합할 수 있다.
- 검색 조건이 선택적으로 적용되는 경우 동적으로 쿼리를 구성할 수 있다.
- `JpaSpecificationExecutor`를 활용하면 Specification을 이용한 조회 기능을 Repository에서 사용할 수 있다.
- 여러 선택 조건을 조합해야 하는 검색 기능에서는 SpecBuilder와 같은 별도 구성 방식을 활용할 수 있다.

### 25. 정리

Spring Data JPA를 활용하면 Repository 인터페이스와 메서드 이름 규칙을 통해 데이터베이스 접근 코드를 간결하게 구현할 수 있다. 기본적인 CRUD와 단순 조회는 Spring Data JPA가 제공하는 기능을 활용하고, 복잡한 검색 조건은 `@Query`, Specification 등을 상황에 맞게 선택하는 것이 중요하다.

이번 4주차 스터디의 핵심은 JPQL과 Criteria를 이용한 데이터 조회 방식의 차이를 이해하고, Spring Data JPA를 통해 Repository 구현을 간소화하는 방법을 익히는 것이다. 특히 메서드 이름 기반 조회, 정렬 및 페이징, Specification을 이용한 동적 검색 조건 처리가 주요 학습 내용이다.
