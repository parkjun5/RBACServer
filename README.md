RBAC 서버 

![유즈케이스 (2)](https://github.com/parkjun5/RBACServer/assets/58926619/5b149464-d9fc-42a1-81fa-704b250298de)

### 인터셉터 -> 자바 빈 접근가능 
- Exception Handler 
- 어노테이션을 통한 uri 컨트롤

uri + 커스텀 어노테이션의 enum값
매핑하여서 필터링하는 기능
prehandle 처리

@RestController 중 
특정 어노테이션을 갖고 있는 uri에 권한 체크를 설정해주도록 설정

1. 헤더에서 로그인한 유저를 가져옴
2. 유저의 권한 확인
3. uri에 맞는 권한이 있는지 인증
4. 없으면 exception 있으면 정상 처리
   https://chat.openai.com/
