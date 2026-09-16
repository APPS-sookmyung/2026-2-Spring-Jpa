\[SpringJPA 1주차 스터디]

1. 프로젝트 세팅하기
* spring initializer를 사용하여 프로젝트 생성
* lombook사용 시 설정의 annotation processer에서 활성화해줘야한 (@Getter, @Setter 등의 사용이 용이해진다)



2\. view 환경설정

* thymeleaf 템플릿 엔진 (thymeleaf 공식 사이트와 Spring 공식 튜토리얼 참고)
* 마크업을 깨지 않고 그대로 사용 가능하다는 장점 존재
* HelloController작성 후 resources의 templates에 hello.html문서 작성을 통해 화면 구성
* controller에 return "hello";로 어떻게 templates의 hello.html을 찾는걸까? 

  * 해당역할은 spring의 thymeleaf가 수행한다. (자동 매핑을 해준다/ 랜더링)
  * 랜더링을 하지 않는다면 static에 html파일을 만들면 된다.
* implementation 'org.springframework.boot:spring-boot-devtools' 빌드 그랜들에 추가하ㅁ으로써 빠른 수정 반영이 가능하다.



3\. H2데이터베이스 

* jdbc:h2:\~/jpashop (으로 맨 처음 연결 실행)
* jdbc:h2:tcp://localhost/\~/jpashop (이후엔 해당 경로로 자유롭게 접근 가능)



4\. JPA 저장 및 조회 테스트

* @Transactional : 테스트를 하나의 트랜잭션 안에서 실행
* @Rollback(false) : 테스트 종료 후 DB 변경사항 유지
* em.persist(member) : 객체를 영속성 컨텍스트에 저장
* em.find() : ID(PK)를 이용해 엔티티 조회
* assertEquals() : 저장한 값과 조회한 값이 같은지 확인
* 같은 트랜잭션에서 조회 시 1차 캐시 사용
* member == findMember → true : 같은 영속성 컨텍스트에서는 동일한 객체 반환

