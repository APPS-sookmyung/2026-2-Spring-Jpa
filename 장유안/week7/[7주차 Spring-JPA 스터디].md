#### \[7주차 Spring-JPA 스터디]

#### 

##### 회원 도메인

* **회원 가입** — MemberService의 join()에서 이름이 같은 회원이 있는지 먼저 확인. 중복이면 IllegalStateException 발생, 중복이 없으면 persist()로 저장 후 생성된 ID 반환
* **회원 조회** — MemberRepository에서 ID로 한 명을 찾을 때는 em.find(), 전체 회원이나 특정 이름의 회원을 찾을 때는 JPQL 사용
* **중복 가입 방지** — 동시에 두 가입 요청이 들어오면 둘 다 중복 검사를 통과할 수 있으므로, 이름 중복을 막는 규칙을 DB에서도 보장하도록 회원명 컬럼에 유니크 제약 조건 추가
* **회원 기능 테스트** — 가입 후 반환된 ID로 회원 조회 확인. 같은 이름으로 다시 가입할 때 assertThrows()로 중복 예외 확인. 테스트 종료 후 트랜잭션 롤백으로 데이터 초기화



##### 상품 도메인

* **재고 관리** — 상품의 재고를 변경하는 로직을 Item 엔티티 안에 작성. 입고나 주문 취소 시 addStock()으로 수량 증가, 주문 시 removeStock()으로 수량 감소
* **재고 부족 처리** — 차감한 수량이 0보다 작으면 NotEnoughStockException 발생. 재고 수량은 검사 통과 후에만 변경하므로 예외 발생 시 기존 값 유지
* **상품 등록과 수정** — 강의 예제에서는 ItemRepository의 save() 하나로 처리. ID가 없으면 신규 상품으로 보고 persist(), ID가 있으면 기존 상품의 수정으로 보고 merge() 호출
* **상품 서비스** — ItemService의 saveItem()은 저장, findItems()는 전체 조회, findOne()은 단건 조회를 리포지토리에 위임



##### 공통 설정

* **트랜잭션** — 서비스 클래스에 @Transactional(readOnly = true)를 붙여 조회를 기본으로 설정. join()과 saveItem()처럼 데이터를 변경하는 메서드에는 별도로 @Transactional 적용
* **생성자 주입** — 리포지토리 필드를 final로 선언하고 @RequiredArgsConstructor로 생성자 자동 생성. 생성자가 하나일 때 @Autowired 생략 가능
* **리포지토리** — @Repository로 스프링 빈 등록. EntityManager를 통해 엔티티 저장과 조회 처리

