# 섹션 2

### [JPA 기초 08 값 콜렉션 Set 매핑]

단순 값을 Set으로 보관하는 모델을 어떻게 매핑할 것인가?

@ElementCollection 어노테이션을 사용하여 매핑

@CollectionTable (

name = “role_perm”, // collectiontable의 이름 지정

joinColumns = @JoinColumn(name = “role_id”) // join할 때 사용할 column 지정

)

@Column // 실제 값을 담고 있는 column

Set<타입 지정>

#### 저장

insert into role_perm (role_id, perm)

#### 조회

-lazy 방식: 연관된 테이블을 나중에 읽어옴

ex) role을 조회하는 시점에 role_perm에 있는 데이터를 함께 조회하는 것이 아니고 role을 조회하고 role_perm에 있는 데이터가 필요할 때 그때 조회

-eager 방식: 즉시 조회

@ElementCollection(fetch = FetchType.EAGER)

role에서 데이터를 읽을 때 role_perm에 있는 데이터도 한 번에 읽어옴

*@ElementCollection fetch 속성 기본값: lazy 

#### set 수정: add(), remove()

insert 쿼리를 이용해 추가

delete 쿼리를 이용해 삭제

#### set 할당

setPermissions 메소드를 이용해 할당

→ delete 쿼리로 모두 지운 후 insert 쿼리로 새로 할당 받은 set의 데이터를 추가

#### set clear()

this.permissions.clear() // set 데이터에 접근

elementcollection set에는 embeddable 타입 set도 사용 가능

→ 매핑 설정 : atcolumn이 사라지고 set의 값으로 embeddable 타입 지정

*embeddable 타입에 Hashest 

정리

-콜렉션 테이블을 이용한 값 Set 매핑

@ElementCollection과 @CollectionTable

### [JPA 기초 09 값 콜렉션 List]

@OrderColumn

리스트의 인덱스 값을 저장할 컬럼 지정

#### 엔티티 삭제

엔티티를 삭제하면 콜렉션 테이블에 있는 값도 같이 삭제

정리

-콜렉션 테이블을 이용한 값 List 매핑

@ElementCollection, @CollectionTable, @OrderColumn

### [JPA 기초 10 값 콜렉션 Map]

@MapKeyColumn으로 column 지정

정리 

콜렉션 테이블을 이용한 값 Map 매핑

@ElementCollecton @CollectionTable @MapKeyColumn

### [JPA 기초 11 값 콜렉션 주의사항]

목록 조회 시 메모리에서 페이징 처리

콜렉션과 관련한 성능 문제는 JPA만으로는 해결 어려움

→ CQRS : 변경 기능과 조회 기능 분리

명령 모델(상태 변경)과 조회 모델을 구분하면 좋음

### [JPA 기초 12 영속 컨텍스트 & 라이프사이클]

-JPA는 영속 엔티티(객체)를 영속 컨텍스트에 담아 변경 추적

트랜잭션 커밋 시점에 변경 바영

-대량 변경은 JPA로 할 필요 없음

직접 쿼리 실행

-분리됨 상태는 변경을 추적하지 않음

### [JPA 기초 13 엔티티 연관 매핑 시작에 앞서]

연관: 엔티티와 엔티티 간의 연결

엔티티가 다른 엔티티를 필드/프로퍼티로 참조

### [JPA 기초 14 엔티티 간 1-1 단방향 연관 매핑]

*주의

-연관 매핑은 꼭 필요할 때만 사용

조회 기능은 별로 모델을 만들어 구현 (CQRS)

-Embeddable 매핑이 가능하다면 사용

@OneToOne, @JoinColumn 사용

### [JPA 기초 15 엔티티 간 1-N 단방향 연관 매핑]

-콜렉션을 사용한 매핑

Set

List

Map

참조키를 이용한 1-N 관계

@OneToMany, @JoinColumn

@JoinColumn은 ex. 플레이어 입장에서 팀을 참조할 때 사용 

### [JPA 기초 16 엔티티 간 N-1 단방향 연관 매핑]

-참조키를 이용한 N-1 관계

@ManyToOne, @JoinColumn로 매핑

sight( 참조할 사이트) - 저장

### [JPA 기초 17 영속성 전파 & 연관 고려사항]

-영속성 전파: 연관된 엔티티에 영속 상태를 전파

*하지만 특별한 이유가 없다면 사용하지 말 것

-연관 고려사항

-연관 대신 ID 값으로 참조 고려

-조회는 전용 쿼리나 구현 사용 고려 (CQRS)

-엔티티가 아닌 벨로인지 확인 (1-1. 1-N 관계)

-1-N < N-1

-양방향 X