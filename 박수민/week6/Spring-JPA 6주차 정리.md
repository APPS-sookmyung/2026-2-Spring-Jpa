# Spring-JPA 6주차

## 

## 회원 리포지토리 개발

```
package jpabook.jpashop.repository;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
@Repository
public class MemberRepository {
    @PersistenceContext
    private EntityManager em;
    public void save(Member member) {
        em.persist(member);
    }
```

```
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
            .getResultList();
    }
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name",
Member.class)
            .setParameter("name", name)
            .getResultList();
    }
}
```

- @Repository 리포지토리를 스프링 빈으로 등록하고, JPA 예외를 스프링 예외로 변환
- @PersistenceContext: JPA의 EntityManager를 주입받기 위한 어노테이션
- EntityManager em: JPA를 통해 데이터를 저장하거나 JPQL를 사용해 조회하는 객체
- @PersistenceUnit: 엔티티 매니저 팩토리 주입
- findAII()과 findByName()에서는 createQuery()를 사용해 JPQL을 작성(*JPQL은 데이터베이스 테이블이 아닌 엔티티 객체를 대상으로 조회하는 쿼리 언어)

## 회원 서비스 개발

회원가입, 중복 회원 검증, 회원 조회 기능

```
@Service
@Transactional(readOnly = true)
public class MemberService {
    @Autowired
    MemberRepository memberRepository;
```

## 주요 어노테이션

- @Service: 회원 서비스 클래스를 스프링 빈으로 등록
- @Transactional: 트랜잭션을 관리하며, 데이터 변경 작업이 하나의 트랜잭션 안에서 처리되도록 함
- Transactional(readOnly = true): 조회 전용 작업에 적용하여 불필요한 플러시를 줄이고 성능 최적화
- @Autowired: 스프링이 관리하는 MemberRepository 객체를 자동으로 주입
(@Autowired를 사용한 필드 주입 방식으로 시작, 이후 생성자 주입 방식 권장)

## 회원 서비스 개발

회원가입, 중복 회원 검증, 회원 조회 기능

```
@Transactional 
public Long join(Member member) {
    validateDuplicateMember(member); 
    memberRepository.save(member);
    return member.getId();
}
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers =
memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재한느 회원입니다.");
        }
    }
```

fineMembers() / findOne() - 회원 조회전체 회원 목록 조회 및 회원 ID를 이용한 단건 조회
join() - 회원가입
중복 회원 검증 후 회원 저장하고 ID 반환
validateDuplicateMember() - 중복 회원 검증
동일한 이름의 회원이 존재하면 예외 발생

```
public List<Member> findMembers() {
    return memberRepository.findAll();
}
public Member findOne(Long memberId) {
    return memberRepository.findOne(memberId);
}
```

## 회원 기능 테스트

- @SpringBootTest : 스프링 환경에서 테스트 실행
- @Transactional : 테스트 종료 후 데이터 롤백
- H2 메모리 DB : 실제 DB와 분리된 테스트 환경 구성

## 상품 리포지토리 개발

```
public void save(Item item) {
    if (item.getId() == null) {
        em.persist(item);
    } else {
        em.merge(item);
    }
}
```

[1](about:blank#fn1)
## 상품 서비스 개발

```
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository
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
```

## 주요 어노테이션

- @Service : 상품 서비스 클래스를 스프링 빈으로 등록
- @Transactional(readOnly = true) : 조회 작업에 읽기 전용 트랜잭션 적용
- @RequiredArgsConstructor : 필수 필드에 대한 생성자를 자동 생성하여 리포지토리 주입

---

1. [↩︎](about:blank#fnref1)
    - save()
    id가 없으면 신규로 보고 persist() 실행 → 신규 상품을 영속성 컨텍스트에 저장
    id가 있으면 이미 데이터베이스에 저장된 엔티티를 수정한다고 보고, merge() 실행 → 기존 상품의 데이터를 병합하여 수정 처리