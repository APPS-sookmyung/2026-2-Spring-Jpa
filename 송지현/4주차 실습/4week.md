

## 18. JPQL 소개

### JPQL이란?

**JPQL(JPA Query Language)**은 JPA에서 사용하는 쿼리 언어이다.

SQL과 문법은 비슷하지만 중요한 차이가 있다.

```text
SQL  → 테이블과 컬럼을 대상으로 조회
JPQL → 엔티티와 엔티티의 속성을 대상으로 조회
```

예를 들어 SQL에서는

```sql
SELECT * FROM review
WHERE hotel_id = 'H-001';
```

처럼 작성하지만 JPQL에서는

```java
SELECT r FROM Review r
WHERE r.hotelId = :hotelId
```

처럼 **테이블명이 아니라 `Review` 엔티티**, 컬럼명이 아니라 `hotelId` 속성을 사용한다.

---

### 기본 사용

```java
TypedQuery<Review> query = em.createQuery(
    "select r from Review r",
    Review.class
);

List<Review> reviews = query.getResultList();
```

- `createQuery()` → JPQL 생성
- `TypedQuery` → 결과 타입이 정해진 쿼리
- `getResultList()` → 쿼리를 실행하고 결과를 List로 조회

---

### 파라미터 사용

```java
TypedQuery<Review> query = em.createQuery(
    "select r from Review r where r.hotelId = :hotelId",
    Review.class
);

query.setParameter("hotelId", "H-001");
```

`:hotelId`에 실제 값을 바인딩한다.

```text
:hotelId
   ↓
"H-001"
```

---

### 정렬

```java
select r
from Review r
order by r.id desc
```

- `ASC` → 오름차순
- `DESC` → 내림차순
- 기본은 오름차순

---

### 페이징

```java
query.setFirstResult(8);
query.setMaxResults(4);
```

- `setFirstResult()` → 몇 번째부터 가져올지
- `setMaxResults()` → 최대 몇 개 가져올지
- 시작 위치는 `0`부터

위 코드는 **9번째 데이터부터 4개를 조회**한다.

### 핵심

```text
JPQL
= SQL과 비슷하지만
테이블이 아닌 "엔티티"를 대상으로 쿼리
```

복잡한 조인이나 DBMS 전용 기능, 통계·대량 조회 등은 강의에서 일반 SQL 사용도 함께 고려하면 좋다.

---

# 19. Criteria 소개

## Criteria란?

JPQL처럼 문자열로 쿼리를 작성하는 것이 아니라 **자바 코드로 쿼리를 만드는 API**이다.

```text
JPQL
"select r from Review r where ..."

        ↓

Criteria
자바 코드로 조건 구성
```


### 기본 흐름

```java
CriteriaBuilder cb = em.getCriteriaBuilder();

CriteriaQuery<Review> cq =
        cb.createQuery(Review.class);

Root<Review> root =
        cq.from(Review.class);

cq.select(root);

TypedQuery<Review> query =
        em.createQuery(cq);
```



---

### 검색 조건

```java
Predicate condition =
        cb.equal(root.get("hotelId"), "H-001");

cq.where(condition);
```

`Predicate`는 **검색 조건**이라고 생각하면 된다.

조건을 여러 개 합치는 것도 가능하다.

```java
Predicate p1 = ...;
Predicate p2 = ...;

Predicate condition = cb.and(p1, p2);
```

또는

```java
cb.or(p1, p2);
```

를 사용할 수 있다.

---

### Criteria의 장점

조건에 따라 쿼리를 동적으로 만들기 쉽다.

```java
Predicate p = cb.conjunction();

if (hotelId != null) {
    p = cb.and(
        p,
        cb.equal(root.get("hotelId"), hotelId)
    );
}

if (mark >= 0) {
    p = cb.and(
        p,
        cb.ge(root.get("mark"), mark)
    );
}

cq.where(p);
```

즉 검색 조건이 있을 때만 조건을 추가할 수 있다.

### 핵심

```text
JPQL
→ 문자열로 쿼리 작성

Criteria
→ 자바 코드로 쿼리 작성
→ 동적 검색 조건에 유용
```


---

# 20. JPA 기타 기능

## 1. AttributeConverter

**Java 타입과 DB 타입을 서로 변환**할 때 사용한다.

예를 들어 Java에서는

```java
boolean opened;
```

을 사용하지만 DB에서는

```text
Y / N
```

으로 저장하고 싶은 경우이다.

```text
Java        DB

true   →    "Y"
false  →    "N"

"Y"    →    true
"N"    →    false
```

`AttributeConverter`의 주요 메서드:

```java
convertToDatabaseColumn()
```

→ Java 값을 DB 값으로 변경

```java
convertToEntityAttribute()
```

→ DB 값을 Java 값으로 변경

엔티티에서는 `@Convert`를 사용해 적용할 수 있다.

---

## 2. @Formula

SQL 실행 결과를 **엔티티의 특정 속성에 매핑**할 때 사용한다.

```java
@Formula("...")
private String categoryName;
```

주로

- DB 함수 결과
- 서브쿼리 결과
- 계산된 값

등 **조회용 값**을 매핑할 때 사용한다.

Hibernate에서 제공하는 기능이다.

---

## 3. @DynamicUpdate

기본 UPDATE는 여러 컬럼을 함께 포함할 수 있는데,

```java
@DynamicUpdate
```

를 사용하면 **실제로 변경된 컬럼 중심으로 UPDATE SQL을 생성**한다.

```text
name만 변경

기본
→ 여러 컬럼 UPDATE

@DynamicUpdate
→ name 중심으로 UPDATE
```

---

## 4. @DynamicInsert

```java
@DynamicInsert
```

는 INSERT 시 **null이 아닌 컬럼 중심으로 SQL을 생성**한다.

단, DB 기본값과 null 처리 의도가 있는 경우에는 주의해야 한다.

---

## 5. @Immutable

```java
@Immutable
```

엔티티를 **변경 추적 대상에서 제외**할 때 사용한다.

주로

```text
수정 필요 X
조회 전용 O
```

인 엔티티에 사용한다.

---

## 6. @Subselect

특정 SELECT 결과를 **엔티티처럼 매핑**하고 싶을 때 사용한다.

```text
SELECT 결과
     ↓
조회 전용 Entity
```

테이블 자체보다는 **뷰(View)와 비슷한 조회 결과**를 엔티티로 표현하는 느낌이다.

보통 수정 대상이 아니므로 `@Immutable`과 함께 사용한다.

---

# Spring Data JPA

# 01. Spring Data JPA 시작하기

지금까지는 직접

```java
EntityManager
em.persist()
em.find()
em.remove()
```

등을 사용했다.

Spring Data JPA를 사용하면 **Repository 인터페이스 중심으로 훨씬 간단하게 구현**할 수 있다.

Spring Boot와 같이 사용하면

- `persistence.xml`
- EntityManagerFactory 초기화
- Spring 트랜잭션 연동
- EntityManager 연동

등 상당 부분을 자동으로 처리한다.

---



예:

```java
public interface UserRepository
        extends Repository<User, String> {

    Optional<User> findById(String email);

    User save(User user);
}
```

우리가 구현 클래스를 직접 만들지 않아도 **Spring Data JPA가 Repository 구현체를 만들어준다.**

### 핵심

```text
기존 JPA
→ EntityManager를 직접 사용

Spring Data JPA
→ Repository 인터페이스 작성
```


---

# 02. Repository 메서드 작성 규칙

Spring Data JPA는 **메서드 이름을 분석해서 쿼리를 만들어준다.**

## ID로 조회

```java
Optional<User> findById(String email);
```

결과가 없으면 `Optional.empty()`를 이용할 수 있다.

---

## 저장

```java
User save(User user);
```

Spring Data JPA의 `save()`는 엔티티가 새로운 엔티티인지 판단한다.

```text
새 엔티티
→ persist()

기존 엔티티
→ merge()
```

즉 `save()`가 무조건 `persist()`만 실행하는 것은 아니다.

---

## 삭제

```java
void delete(User user);

void deleteById(String email);
```

---

## 조건으로 조회

메서드 이름만으로 조건을 만들 수 있다.

```java
List<User> findByName(String name);
```

의미:

```sql
where name = ?
```

여러 조건:

```java
findByGradeAndName(grade, name)
```

### 다양한 조건

```java
findByNameLike(...)
findByCreatedAtAfter(...)
findByYearBetween(...)
```

그 외에도

```text
LessThan
IsNull
Containing
In
```

등의 키워드를 사용할 수 있다.

### 핵심

```text
findBy + 필드명 + 조건
```

다만 조건이 복잡해지면 메서드 이름이 너무 길어지므로 `@Query`, Specification 등 다른 방법을 고려한다.

---

# 03. 정렬, 페이징, @Query

## 정렬 방법 1 - 메서드 이름

```java
findByNameLikeOrderByNameDesc(keyword);
```

```text
OrderByNameDesc
→ name 기준 내림차순
```

여러 개도 가능하다.

```java
OrderByNameAscEmailDesc
```

---

## 정렬 방법 2 - Sort

```java
Sort sort =
    Sort.by(Sort.Order.asc("name"));

userRepository.findByNameLike(
    "이름%",
    sort
);
```

이 방법은 정렬 조건을 실행할 때 전달할 수 있다는 장점이 있다.

---

## 페이징

`Pageable`을 사용한다.

```java
List<User> findByNameLike(
    String keyword,
    Pageable pageable
);
```

실제 객체는 `PageRequest` 등을 이용해 생성한다.

```java
Pageable pageable =
    PageRequest.ofSize(10)
               .withPage(1);
```

**페이지는 0부터 시작한다.**

```text
0 → 첫 번째 페이지
1 → 두 번째 페이지
2 → 세 번째 페이지
```

---

## Page

단순히 데이터만 필요한 것이 아니라

- 전체 데이터 개수
- 전체 페이지 수
- 현재 페이지
- 현재 데이터

등도 필요하다면 `Page`를 사용할 수 있다.

```java
Page<User> findByEmailLike(
    String keyword,
    Pageable pageable
);
```

사용:

```java
page.getTotalElements();
page.getTotalPages();
page.getContent();
page.getSize();
page.getNumber();
```

---

## @Query

메서드 이름 규칙 대신 **JPQL을 직접 작성**할 수도 있다.

```java
@Query("""
    select u
    from User u
    where u.createdAt > :since
""")
List<User> findRecentUsers(
    @Param("since") LocalDateTime since
);
```

장점:

```text
findBy어쩌고저쩌고And어쩌고...
```

처럼 메서드 이름이 길어지는 것을 줄일 수 있다.

### 핵심


Sort : 정렬

Pageable: 페이징

Page: 페이징 정보까지 반환

@Query: 직접 JPQL 작성



---

# 04. Specification을 이용한 검색 조건 지정

`Specification`은 **검색 조건을 객체로 표현하기 위한 인터페이스**이다.

내부적으로 Criteria를 이용한다.

```java
public interface Specification<T> {

    Predicate toPredicate(
        Root<T> root,
        CriteriaQuery<?> query,
        CriteriaBuilder builder
    );
}
```


---

## Repository

```java
List<User> findAll(
    Specification<User> spec
);
```

Specification을 전달하면 해당 조건으로 데이터를 조회한다.

---

## Specification 만들기

```java
public static Specification<User> nameLike(
        String value) {

    return (root, query, cb) ->
        cb.like(
            root.get("name"),
            "%" + value + "%"
        );
}
```

사용:

```java
Specification<User> spec =
        UserSpecs.nameLike("지현");

List<User> users =
        userRepository.findAll(spec);
```

---

## 조건 조합

Specification의 장점 중 하나는 **조건을 조합하기 쉽다는 것**이다.

```java
Specification<User> spec =
        UserSpecs.nameLike("이름")
        .and(
            UserSpecs.createdAfter(dateTime)
        );
```

`and`, `or`를 이용할 수 있다.

또 조건이 존재할 때만 추가하는 방식으로 **동적 검색 조건**을 구성할 수 있다.

```text
이름 입력됨
→ 이름 조건 추가

날짜 입력됨
→ 날짜 조건 추가

둘 다 입력됨
→ 이름 AND 날짜
```

정렬이나 페이징과 함께 사용할 수도 있다.

### 핵심

```text
Criteria
→ 코드로 쿼리 만들기

Specification
→ Criteria 검색 조건을 객체화
→ 여러 조건 조합
```


---

# 05. Spring Data JPA 기타

## count()

데이터 개수를 조회할 수 있다.

```java
long count();
```

조건을 붙일 수도 있다.

```java
long countByNameLike(String keyword);
```

Specification:

```java
long count(Specification<User> spec);
```

---

## Native Query

`@Query`에서 JPQL이 아닌 **진짜 SQL**을 직접 사용할 수도 있다.

```java
@Query(
    value = "select * from user ...",
    nativeQuery = true
)
```

```text
@Query
+ nativeQuery = true
→ SQL 직접 실행
```

DB에 특화된 기능이 필요할 때 사용할 수 있다.

---

## 결과 한 개 조회

```java
User findByName(String name);
```

또는

```java
Optional<User> findByName(String name);
```

결과가 없을 가능성이 있다면 `Optional`을 이용할 수 있다.

단, **단건을 기대했는데 결과가 여러 개라면 예외가 발생**한다.

---

## Repository 하위 인터페이스

Spring Data에서는 다음과 같은 인터페이스도 제공한다.

```text
Repository
   ↑
CrudRepository
PagingAndSortingRepository
JpaRepository
...
```

상위 기능이 많은 인터페이스를 사용하면 기본 CRUD 등의 메서드를 바로 사용할 수 있어 편리하다.

강의에서는 필요한 기능만 노출하고 싶다면 기본 `Repository`를 상속하고 **필요한 메서드만 선언하는 방식**도 소개한다.

---



