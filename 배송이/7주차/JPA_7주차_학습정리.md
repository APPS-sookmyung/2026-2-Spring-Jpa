# JPA 7주차 학습 정리

이번 주에는 회원과 상품 도메인의 기본 기능을 구현했다. 엔티티를 직접 조회·저장하는 **리포지토리**, 업무 규칙과 트랜잭션을 관리하는 **서비스**, 동작을 확인하는 **테스트**의 역할을 구분하는 데 초점을 맞췄다.

## 1. 회원 도메인 구현

### 1.1 회원 리포지토리

`MemberRepository`는 JPA의 `EntityManager`를 이용해 회원 데이터를 저장하고 검색한다.

```java
package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery(
                        "select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
```

`@Repository`는 해당 클래스를 스프링의 데이터 접근 계층 컴포넌트로 등록하며, 적용 가능한 영속성 예외를 스프링의 데이터 접근 예외 체계로 변환하는 데 관여한다. `@PersistenceContext`는 컨테이너가 관리하는 엔티티 매니저를 주입받는 데 사용한다. 반면 `@PersistenceUnit`은 엔티티 매니저 자체가 아닌 `EntityManagerFactory`를 주입할 때 사용한다.

| 메서드 | 역할 |
| --- | --- |
| `save()` | `persist()`로 새 회원을 영속성 컨텍스트에 등록 |
| `findOne()` | 기본 키를 이용해 회원 한 명 조회 |
| `findAll()` | JPQL로 전체 회원 목록 조회 |
| `findByName()` | 이름을 조건으로 회원 검색 (`:name`에 값 바인딩) |

### 1.2 회원 서비스

서비스 계층은 리포지토리를 활용하면서 회원 가입 시 이름 중복 여부와 같은 업무 규칙을 처리한다.

```java
package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> existingMembers = memberRepository.findByName(member.getName());
        if (!existingMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
```

`@Service`는 서비스 클래스를 스프링 빈으로 등록한다. `@Transactional`은 메서드 실행을 트랜잭션 경계 안에서 관리한다. 클래스에는 `readOnly = true`를 설정해 조회 중심 메서드의 의도를 나타내고, 실제로 데이터를 저장하는 `join()`에는 별도의 `@Transactional`을 붙여 읽기 전용 설정을 덮어썼다. 읽기 전용 트랜잭션은 JPA 구현체의 변경 감지·플러시 관련 최적화나 데이터베이스의 읽기 최적화에 도움이 될 수 있지만, 구체적인 효과는 환경에 따라 달라진다.

의존성 주입은 필드에 `@Autowired`를 붙이는 방식도 가능하지만, 예제에서는 `final` 필드와 Lombok의 `@RequiredArgsConstructor`를 이용해 생성자 주입으로 구성했다. 스프링에서는 생성자가 하나라면 생성자에 `@Autowired`를 생략할 수 있다.

회원 가입 과정은 **이름 중복 조회 → 중복 시 예외 발생 → 중복이 없으면 저장 → 회원 ID 반환** 순서로 이루어진다. 단, 이름 중복 검사를 애플리케이션에서만 수행하면 동시 가입 요청에서 경쟁 조건이 발생할 수 있으므로 실제 서비스에서는 데이터베이스의 유니크 제약조건도 고려해야 한다.

### 1.3 회원 기능 테스트

회원 저장과 중복 가입 방지 로직이 의도대로 동작하는지 통합 테스트로 확인한다.

```java
package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @Test
    void 회원가입() {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        Long savedId = memberService.join(member);

        // then
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test
    void 중복_회원_예외() {
        // given
        Member first = new Member();
        first.setName("kim");
        Member second = new Member();
        second.setName("kim");

        // when
        memberService.join(first);

        // then
        assertThrows(IllegalStateException.class,
                () -> memberService.join(second));
    }
}
```

`@SpringBootTest`는 스프링 부트 애플리케이션 컨텍스트를 구성하여 실제 빈들을 연결한 상태로 테스트할 수 있게 한다. 이 예제에서 이를 제거하면 별도의 테스트 설정이 없는 한 `@Autowired`로 주입할 빈이 준비되지 않는다. 테스트 클래스의 `@Transactional`은 각 테스트를 트랜잭션 안에서 실행하고, 기본적으로 종료 시 롤백하므로 테스트 데이터가 다음 테스트에 남지 않도록 돕는다.

첫 테스트는 저장한 회원을 ID로 다시 찾았을 때 동일한 영속성 컨텍스트의 회원 객체가 반환되는지 검사한다. 두 번째 테스트는 같은 이름으로 다시 가입할 경우 `IllegalStateException`이 발생하는지 확인한다. 참고로 첫 테스트의 객체 동일성만으로 데이터베이스에 SQL INSERT가 실제 반영되었음을 검증하는 것은 아니다. 필요하면 `flush()`와 `clear()`를 사용해 DB 반영 및 재조회를 따로 확인할 수 있다.

## 2. 상품 도메인 구현

### 2.1 상품 엔티티와 재고 관리

상품의 공통 속성은 추상 클래스 `Item`에 정의한다. 상속 구조는 단일 테이블 전략으로 매핑하며, 재고 변경은 엔티티 내부의 메서드에서 처리한다.

```java
package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int remaining = this.stockQuantity - quantity;
        if (remaining < 0) {
            throw new NotEnoughStockException("재고가 부족합니다.");
        }
        this.stockQuantity = remaining;
    }
}
```

`@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`는 하위 상품 타입을 하나의 테이블에 저장하도록 지정한다. `@DiscriminatorColumn`은 각 행이 어떤 하위 엔티티 타입인지 구분하는 컬럼을 정의한다. `@ManyToMany(mappedBy = "items")`는 상품과 카테고리 사이의 다대다 관계에서 상품 쪽이 연관관계의 주인이 아님을 나타낸다.

재고를 변경하는 메서드를 엔티티에 두면 재고 계산과 검증 규칙을 한곳에서 관리할 수 있다. `addStock()`은 수량을 더하고, `removeStock()`은 차감 결과가 음수가 되는 경우 사용자 정의 예외를 발생시켜 부족한 재고로 출고되는 상황을 방지한다. 이 예제는 전달받는 수량이 양수라고 가정하므로, 실무에서는 0이나 음수 입력에 대한 검증도 추가하는 것이 좋다.

재고 부족을 표현하는 예외는 다음과 같이 작성한다.

```java
package jpabook.jpashop.exception;

public class NotEnoughStockException extends RuntimeException {

    public NotEnoughStockException() {
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }
}
```

`RuntimeException`을 상속하므로 체크 예외처럼 호출부에서 반드시 선언하거나 처리할 필요는 없다. 스프링의 기본 트랜잭션 설정에서는 이러한 런타임 예외가 발생하면 트랜잭션이 롤백된다.

### 2.2 상품 리포지토리

상품 리포지토리는 저장과 단건·전체 조회 기능을 제공한다.

```java
package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
```

`save()`는 ID가 `null`이면 신규 상품으로 간주해 `persist()`를 실행하고, ID가 존재하면 `merge()`를 호출한다. 여기서 주의할 점은 **ID가 있다는 사실만으로 엔티티가 실제 DB에 존재한다고 보장되지는 않는다**는 것이다. 또한 `merge()`는 전달한 객체를 그대로 영속화하는 것이 아니라 병합 결과로 관리되는 엔티티를 반환한다. 따라서 실제 수정 로직에서는 기존 엔티티를 조회한 뒤 필요한 필드를 변경하는 **변경 감지(dirty checking)** 방식이 일반적으로 더 안전하다.

`findOne()`은 기본 키 조회, `findAll()`은 JPQL을 이용한 전체 조회를 수행한다. `@RequiredArgsConstructor`를 통해 `final EntityManager`를 생성자에서 주입받는다.

### 2.3 상품 서비스

현재 상품 서비스에는 별도의 복잡한 업무 규칙이 없으므로 리포지토리의 기능을 호출하는 형태로 구성한다.

```java
package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
```

`saveItem()`은 쓰기 작업이므로 메서드에 `@Transactional`을 별도로 지정한다. `findItems()`와 `findOne()`은 클래스 수준의 읽기 전용 트랜잭션 설정을 따른다. 지금은 서비스가 리포지토리 호출을 중계하는 역할에 가깝지만, 향후 상품 등록 정책이나 수정 규칙이 추가되면 해당 비즈니스 로직을 서비스 계층에서 조합할 수 있다.

## 3. 총정리

| 구분 | 주요 책임 | 기억할 내용 |
| --- | --- | --- |
| 회원 리포지토리 | 회원 저장 및 조회 | `EntityManager`, JPQL, 파라미터 바인딩 |
| 회원 서비스 | 가입 절차와 중복 검사 | `@Transactional`, 생성자 주입 |
| 회원 테스트 | 정상 가입과 예외 검증 | `@SpringBootTest`, `assertThrows`, 테스트 롤백 |
| 상품 엔티티 | 상품 정보 및 재고 규칙 | 상속 매핑, 재고 부족 예외 |
| 상품 리포지토리 | 상품 저장 및 조회 | `persist()`와 `merge()`의 차이 |
| 상품 서비스 | 상품 작업의 트랜잭션 경계 | 조회 전용과 쓰기 트랜잭션 분리 |

이번 학습에서 중요한 점은 **데이터 접근은 리포지토리, 업무 흐름과 트랜잭션은 서비스, 데이터 자체의 상태 변경 규칙은 엔티티**에 배치하는 구조다. 계층마다 책임을 구분하면 기능을 추가하거나 테스트할 때 변경 범위를 파악하기 쉬워진다.
