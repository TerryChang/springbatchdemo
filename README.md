이 Repository는 Spring Boot를 이용한 Spring Batch를 공부하기 위해 만든 Repository로서 Spring Boot, Spring Data JPA
, Spring Batch를 사용했습니다.
컨셉은 로그 파일을 읽어 DB에 Summary를 저장하는 구조로 되어 있습니다. 로그 파일은 제 logbackdemo Repository에서 작성하는 
로그파일을 기반으로 이를 summary 한 내용을 저장하는 배치로 보시면 되겠습니다.
소스에 대한 대략적인 설명은 이정도로 하고 이 이후의 내용은 공부하면서 얻은 내용을 두서없이 정리한 것으로 존칭어 사용안하
고 작성합니다. 일단은 이렇게 정리하고 좀더 안정화가 되면 제 블로그에 관련 내용을 더 정리하도록 하겠습니다

#### **1. @ManyTonOne의 Eager fetch mode에 대하여...**

흔히 알고 있는 내용은 @ManyToOne 어노테이션 사용시 fetch = FetchType.EAGER로 사용하면 별도로 언급을 안해도 조회할때 관련
 객체를 같이 조회하는 것으로 알려져 있다. 그러나 이러한 이해때문에 혼선이 생기는 지점이 있어서 정리해둔다. 예를 들어 다음과 
 같은 엔티티가 있다고 가정해보자(디테일한 문법은 생략했고 설명에 있어서 필요한 문법만 챙겼기 때문에 이 코드를 그대로 실습에서
 사용하지는 말자)

```java
 public class Member {
    
    private Long idx;
    
    private String name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TEAM_IDX", foreignKey = @ForeignKey(name = "FK_MEMBER_TEAM"), nullable = false)
    Team team;
    
    ...
 }
 
 public class Team {
   private Long idx;
   
   private String teamName;
 }
``` 

 회원과 그 회원이 속한 팀과의 관계는 ManyToOne이다. 이때 회원을 조회할때 그 회원이 속한 팀의 정보도 같이 조회한다고 가정해보자
 우리가 Hibernate 구현체를 이용한 JPA에서 이를 조회할때 EntityManager 클래스가 제공하는 find 메소드에 Member 엔티티 객체의 
 idx 값을 넘겨줌으로써 이를 조회할 수 있다
 
 ```java
 Member member = entityManager.find(Member.class, 10);
 ```

 이렇게 조회할 경우 실행되는 실제 SQL는 다음과 같은 형태로 실행이 된다(이해를 위해 내용 전달이 되게끔 SQL을 작성한 것이지 
 실제 hibernate가 위의 find 메소드를 실행시켰을때 출력하는 SQL문과는 완전 똑같은 형태는 아니다)
 
```sql
 select m.idx, m.name, t.idx, t.teamName from Member m, Team t where m.idx = 10 and m.teamidx = t.idx
``` 

 이렇게 Member 테이블과 Team 테이블을 join을 걸어서 한번에 이를 조회하고 있다
 그러면 이것과 같은 결과를 조회하기 위해 jpql문으로 다음과 같이 조회한다면 어떻게 될까?

```jpaql 
 select m from Member m where m.idx = 10;
```
 
 이렇게 jpql 문으로 작성하여 이를 조회할 경우 Member 엔티티 안에 있는 Team 엔티티 객체는 @ManyToOne에 fetchType은 EAGER로
  설정되어 있기 때문에 jpql에서 Team 엔티티에 대한 언급을 하지 않더라도 Team 엔티티에 대한 조회를 할 것이다. 근데 위에서 
  언급한 find 문과는 다른 식으로 조회하게 된다. 실제 실행되는 sql 문은 다음과 같다

```sql  
 select idx, name, team_idx from Member where idx = 10;
 select idx, teamName from Team where idx = 5; (여기서 5는 위의 SQL 문에서 조회하여 나온 team_idx 컬럼 값이다)
```
 
 find 문에서는 Member 테이블과 Team 테이블을 join 을 걸어서 이를 진행했지만 **jpql로 조회할 경우엔 Member 테이블과 Team 
 테이블을 join 걸지 않고 따로따로 조회**하고 있다. ManyToOne의 관계 설정만 놓고 보면 join 해서 가져오겠지..null을 허용하면 
 left outer join 형태를 취하는 한이 있더라도 join 해서 가져오겠지..라고 생각하고 있겠지만 실제로는 그렇지가 않다. 그래서
 **jpql을 사용해서 조회할 경우에는 EAGER로 되어 있다 하더라도 jpql에서 명시적으로 join을 걸어서 조회**하도록 해야 한다.
 
 만약 Spring Data Jpa를 사용할 경우 **@EntityGraph 어노테이션을 사용**하면 jpql에서 별도로 join을 명시하지 않아도 내부적으로 
 join을 걸어서 가져올 수 있지만 문제는 이렇게 할 경우 **nullable을 false로 해서 null이 없다고 설정을 했는데도 불구하고 
 left outer join으로 가져오기 때문에 퍼포먼스에 문제**가 있게 된다

```java
 @EntityGraph(attributePaths = {"team"}, type = EntityGraph.EntityGraphType.LOAD)
 @Query("select m from Member m where m.idx = :idx ")
 Optional<Member> memberFindById(@Param("idx") Long idx);
``` 

#### **2. Entity에서 LinkedHashSet 을 사용하는 것에 대하여**
 
 Entity에서 중복 객체를 허용하지 않을 경우 Set 인터페이스를 구현한 클래스를 사용하게 되는데 Set의 경우 순서 개념이 없기 
 때문에 순서 개념을 적용할려면 LinkedHashSet 클래스를 설정하여 사용하게 된다. 근데 여기서 오해를 살 지점이 하나 있다.
 위에서 예로 들은 Member와 Team의 경우 Team 엔티티 입장에서는 해당 Team에 속한 Member를 알기위해 다음과 같이 정의된 멤버
 변수가 있을수있다

```java
 @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
 private Set<Member> memberSet = new LinkedHashSet<>();
```

 순서를 고려했기 때문에 LinkedHashSet 클래스 객체로 이를 초기화했다. 이렇게 정의하면 Team 엔티티 객체를 DB에 insert 할 때
 Member 엔티티도 DB에 같이 insert 하기 위해 memberSet 변수에 Member 엔티티 객체를 넣을 경우 객체를 넣은 순서대로 DB에 
 insert 하게 된다(이것은 나름 의미가 있는 것이 DB에 insert 할 때 primary key가 설정되기 때문에 primary key를 통한 정렬을 
 해야 할 경우 이것에 대한 순서를 고려해야 할 상황이 있을수 있기 때문이다)
 
 그러면 insert 할 때가 아니라 Team 엔티티를 조회할때 Member 엔티티가 같이 조회되게끔 하려 할 경우는 어떨까? 이때 다음과 
 같이 jpql을 실행시켜서 Member 엔티티의 idx값으로 정렬된 형태로 Member 엔티티들을 조회하게 될까?
 
 ```jpaql
 select t from Team t inner join fetch t.member m where t.idx = 5 order by m.idx
 ``` 

 sql 상에서는 Member 테이블의 idx 순으로 조회가 되어서 나오지만 그것을 실제 엔티티로 변환하는 과정에서는 순서가 무시된다
 왜냐면 insert 할 때는 내부적으로 LinkedHashSet 클래스 객체 안에 들어가 있는 상태이기 때문에 순서대로 넣게 되지만 **select
 경우엔 hibernate가 엔티티에 초기화한 LinkedHashSet을 보는 것이 아니라 Set 인터페이스를 보고 이에 따라 HashSet을 감싼 
 PersistenceSet 클래스 객체를 생성하여 넣기 때문에 순서가 무시되는 상황이 벌어지게 되기 때문**이다.
 
 그래서 이와 같은 문제를 해결하기 위해서는 다음과 같이 어노테이션을 추가로 설정해주어야 한다
 
 ```java
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  @OrderBy("idx asc")
  private Set<Member> memberSet = new LinkedHashSet<>();
 ``` 
 
 hibernate에서는 해당 field에 @OrderBy 어노테이션이 적용되어 있기 때문에 HashSet이 아닌 **LinkedHashSet을 감싼 PersistenceSet 
 클래스 객체를 생성해서 넣기 때문에 순서를 보장**하게 된다. 만약 위에서 언급했던 jpql에서 order by m.idx가 아니라 order by m.idx desc
 이렇게 역정렬을 하라고 줄 경우엔 어떻게 될까? 그때는 **@OrderBy 어노테이션에 설정한 idx asc는 무시하고 jpql에서 설정한 정렬 기준으로 
 동작**하게 된다. @OrderBy 어노테이션에서 설정하는 정렬 기준은 우리가 EntityManager 클래스의 find 메소드등의 메소드로 엔티티를 조회할때
 그때 사용되는 정렬기준이다(메소드에서는 조회하고자 하는 엔티티의 하위 엔티티에 대한 정렬 기준을 설정하는 방법이 없기 때문에
 저렇게 한 것이다)
  
  
   