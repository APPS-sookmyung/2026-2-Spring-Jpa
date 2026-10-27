# 섹션 4. Spring Data JPA  기초

## 01 시작하기

-JPA를 단독으로 사용하지 않음

Spring Boot + Spring Data JPA (JPA를 Spring에 연동) → 거의 설정 없이 사용

#### 사용법

- Spring-boot-starter-data-jpa 의존 추가
- 스프링 부트 설정
    - **엔티티 단위**로 Repository 인터페이스를 상속 받은 인터페이스 생성
        - Repository 인터페이스: 스프링 데이터 JPA가 제공하는 특별한 타임으로 이 인터페이스를 상속한 인터페이스를 이용해 빈(bean) 객체 생성
        
        ![image.png](image.png)
        
        - 지정한 규칙에 맞게 해당 인터페이스에 추가
        
        ![image.png](image%201.png)
        
        - 필요한 곳에 해당 인터페이스 타입을 주입해서 사용
        
- Spring.jpa.database=연동 대상 DB (ex. mysql)
    
    * 스프링 부트 버전에 따라 설정은 달라질 수 있으니 버전에 다른 래퍼런스 문서 참고
    

### → 인터페이스만 정해진 규칙에 따라 알맞게 만들어주면 나머지는 Spring Data JPA가 알아서 처리

## 02 리포지터리 메서드 작성 규칙

#### DB 연동 코드 구현 방법

- Repository 인터페이스를 상속
- 정해진 규칙에 따라 메서드를 추가

#### 이 메서드를 어떻게 작성하는가

- **식별자로 엔티티 조회**
    - findById
        - T findByID(ID id) 없으면 null
        - Optional<T> findByID(ID id) 없으면 empty Optional

- **엔티티 삭제**
    - delete
        - void delete(T entity) : 삭제할 엔티티를 전달 받음
        - void deleteById(ID id) : 엔티티에 해당하는 id값을 전달받음
            
                                                      내부적으로 findById()로 엔티티를 조회한 뒤 delete()로 삭제
            
    - 삭제할 대상이 존재하지 않으면 익셉션 발생

- **엔티티 저장 메서드**
    - save : 저장할 엔티티를 파라미터로 받음)
        - void save(T entity)
        - T save(T entity)
        
        save() 호출시 실행 쿼리
        
        ![image.png](image%202.png)
        

![image.png](image%203.png)

→ merge때문에 select 먼저 실행 후 insert

- @version : 동시성 관련 처리
- 식별자가 참조 타입 (ex. String. integer…)
- 식별자가 숫자 타입 (ex. int, long…)

persist로만 저장해야 하는데도 불구하고 기존에 데이터가 존재해서 merger되는 경우

Persistable 인터페이스를 직접 구현하여 방지 가능

- **특정 조건으로 찾기**
    - findBy프로퍼티(값) : 프로퍼티가 특정 값인 대상
    
    → 특정 프로퍼티의 값이 지정한 값과 같은 엔티티 목록 조회
    
    - List<User> findByName(String name) : User 엔티티에 있는 Name 속성의 값 = 파라미터로 준 Name
    - List<Hotel> findByGradeAndName(Grade g. String name)
    - 다양한 조건 비교 (Like, After, Between…)
    - findAll() : 모두 조회
    

findBy 메소드는 검색 조건이 단순할 때만!

## 03 정렬, 페이징, @Query

#### 정렬 1

- find 메서드에 OrderBy 붙임
    - OrderBy 뒤에 프로퍼티명 + Asc/Desc
    - 여러 프로퍼티 지정 가능

#### 정렬 2

- sort 타입 사요어

#### 페이징

- Pageable(=인터페이스)/PageRequest 사용
- withsort를 사용하면 정렬 순서도 함께 지정 가능
- 페이징 조회 결과 Page로 구하기
    - Page 타입: 페이징 처리에 필요한 값을 함께 제공 (전체 페이지 개수, 전체 개수 등)
    - Pageable을 사용하는 메서드의 리턴 타입을 Page로 지정하면 됨

#### @Query

- 메서드 명명 규칙이 아닌 JPQL을 직접 사용 → 메서드 이름이 간결해짐
- sort/pageable을 인자로 줘도 됨

⇒ 메서드 명으로 지정할 순 있지만 가능하면 Sort 사용

Pageable.PageRequest로 페이징 처리 가능

@Query를 사용해서 JPQL 지정

## 04 스펙

- **Specification : 검색 조건을 생성하는 인터페이스**
    - Criteria를 이용해서 검색 조건 생성

스펙 사용

- 리포지토리 : Specificaiton을 이용한 검색 조건 지정
    - List<T> findAll(Specificaton<T> spec) → 스펙을 이용해 검색 조건을 생성하고 그 조건에 해당하는 엔티티를 찾아 리스트로 리턴

#### 람다로 간결하게 구현

- Specification을 구현한 클래스를 매번 만들기 보다는 람다식을 이용해서 스펙 생성

#### 검색 조건 조합

- Specification의 or/and 메서드를 이용해서 조합 → 스펙 인터페이스의 디폴트 구현
- 선택적으로 조합

#### Specification + 페이징, 정렬 가능

#### SpecBuilder

- if 절을 덜 쓰기 위한 SpecBuilder 구현 (보조 클래스)

## 05 기타

#### count 메서드

![image.png](image%204.png)

#### @Query 네이티브 퀴리

- @Query 어노테이션 JPQL이 아닌 SQL 실행
    - SQL이다 지정 > nativeQuery = true

#### 한 개 결과 조회

![image.png](image%205.png)

list 대신