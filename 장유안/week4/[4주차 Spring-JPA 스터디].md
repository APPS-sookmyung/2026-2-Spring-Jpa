#### \[4주차 Spring-JPA 스터디]

##### 

##### JPQL

* 테이블이 아닌 엔티티와 속성을 대상으로 하는 객체 지향 쿼리 언어.
* SQL과 유사하며 WHERE, AND/OR, ORDER BY, 비교 연산자 등을 지원.
* 이름 기반·위치 기반 파라미터로 조회 조건 전달.
* JOIN, 집계 함수, GROUP BY/HAVING, 서브쿼리 등 지원.



##### Criteria API

* 문자열 대신 Java API로 쿼리를 구성하는 방식.
* CriteriaBuilder: 조건 생성
* CriteriaQuery: 쿼리 구성
* Root: 엔티티 속성 접근
* Predicate: 검색 조건 표현
* 제네릭 타입 지정으로 일부 타입 오류를 컴파일 시 발견 가능.



##### 엔티티 매핑 및 Hibernate 기능

|AttributeConverter|Java ↔ DB 값 변환|
|-|-|
|@Formula|SQL 결과를 조회 전용 속성으로 매핑|
|@DynamicUpdate|변경된 컬럼만 UPDATE|
|@DynamicInsert|null 컬럼을 INSERT에서 제외|
|@Immutable|엔티티 변경 감지 제외|


Repository와 CRUD
---

* Repository 인터페이스를 기반으로 구현체 자동 생성.
* findById: ID 조회
* save: 저장 및 병합
* delete: 삭제
* findAll: 전체 조회
* save()는 새 엔티티면 persist(), 기존 엔티티면 merge() 사용.
* 새 엔티티 여부는 isNew(), 버전, 식별자 값 등으로 판단.



##### 메서드 이름 기반 쿼리

* findBy 뒤에 속성과 조건을 붙여 쿼리 자동 생성.
* And/Or: 조건 결합
* Like/Containing: 문자열 검색
* After/LessThan/Between: 범위 비교
* IsNull/In: NULL 및 포함 여부
* 단일 결과는 객체 또는 Optional로 반환하며, 결과가 여러 개면 예외 발생.



##### 정렬·페이징·@Query

* OrderBy, Sort: 오름차순·내림차순 정렬.
* Pageable, PageRequest: 페이지 번호(0부터 시작), 크기, 정렬 지정.
* Page: 조회 데이터와 전체 개수·페이지 수 제공.
* @Query: JPQL 직접 작성, @Param: 파라미터 연결.
* findTop/findFirst: 조회 결과 개수 제한.



##### Specification

* Criteria 기반 검색 조건을 재사용하고 동적으로 조합하는 인터페이스.
* toPredicate(): 검색 조건 생성.
* and()/or(): 조건 결합.
* JpaSpecificationExecutor: 조건 기반 조회 및 집계 지원.
* SpecBuilder: 조건 조합을 간소화하는 사용자 정의 유틸리티.



##### 기타 Repository 기능

* count(): 전체 개수 조회.
* countBy...(): 특정 조건에 해당하는 개수 조회.
* count(spec): Specification 조건별 개수 조회.
* nativeQuery = true: JPQL 대신 SQL 실행.

|인터페이스|주요 기능|
|-|-|
|Repository|기본 인터페이스|
|CrudRepository|CRUD|
|PagingAndSortingRepository|정렬·페이징|
|JpaRepository|CRUD·정렬·페이징 및 JPA 기능|



