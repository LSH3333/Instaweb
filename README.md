# Instaweb

## 배포 주소 
(https://lsh-instaweb.herokuapp.com/)

## 도메인 모델 
<img src="https://github.com/LSH3333/Instaweb/assets/62237852/b1cd4c26-9f62-4473-9a16-f53f87c0ce3e.png" width="60%" height="60%"/>


## 주요 기능 
### 회원가입
<img src="https://github.com/LSH3333/Instaweb/assets/62237852/54c70dd1-6090-4812-a09f-be42bd660e57.png" width="60%" height="60%"/>

### 로그인
<img src="https://github.com/LSH3333/Instaweb/assets/62237852/ad5c3a10-c97f-482b-bb90-592edf075ee4.png" width="60%" height="60%"/>

로그인 여부 확인은 Spring Interceptor 통해서 진행.

모든 경로 막아 놓고 로그인 불필요한 경로(home 화면, 글 보기, ajax 요청 경로 등) 만 Interceptor 거치치 않도록 함
<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/3055332448adafbf4d22b2a890ca155be510a96b/src/main/java/web/instaweb/interceptor/LoginCheckInterceptor.java#L13-L37

https://github.com/LSH3333/Instaweb/blob/3055332448adafbf4d22b2a890ca155be510a96b/src/main/java/web/instaweb/WebConfig.java#L9-L40
  
</details>
<br/>

### 글 작성, 수정  
<img src="https://github.com/LSH3333/Instaweb/assets/62237852/c41861cd-402f-4485-9af9-679f5020c271.png" width="60%" height="60%"/>

글 작성, 수정에서 작성된 모든 내용 (제목,콘텐츠,이미지 등) 들은 모두 ajax 요청으로 서버로 보내서 저장 처리.

수정시 서버에서 해당 글에 저장된 모든것들 불러와서 다시 로드함.    
<br/><br/>
**이미지**는 사용자가 삽입했다가 지웠다가 할 수 있으므로 삽입시 html에 img 태그 삽입과 함께 고유 uuid 부여해서 해당 값을 key 값으로하는 map 에 저장해놓는다. 

이미지를 사용자가 지우면 img 태그가 지워진다.

글 작성이 끝나고 서버에 정보들이 보내질때 img 태그들과 map 을 대조해서 지워지지 않은 이미지들만 서버로 보낸다. 

<br/>

**에디터** 내 복사 붙여넣기 지원. (html element 를 통째로 복사해서 붙여 넣는 방법으로 속성(폰트크기 등)들 유지됨)

외부 프로그램에서 복사 붙여 넣기 시 모두 단순 텍스트로 변형해서 붙여넣어짐.


에디터 기능은 글 작성, 수정에서 동일하게 쓰이므로 thymeleaf fragment 로 만들어서 관리함.
(https://github.com/LSH3333/Instaweb/blob/main/src/main/resources/templates/fragment/editorData.html)

<br/>

### 홈

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/ae2c64be-50d4-4aba-b9cc-14c971c24a8a.png" width="20%" height="20%"/>

홈에서는 모든 유저들이 작성한 글들을 볼 수 있다.

홈과 작성된 글들은 로그인 여부에 상관 없이 볼 수 있다. 

(스프링 인터셉터에서 LoginCheckInterceptor 의 검사 경로에서 제외)

<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/af3e3fab8dc91a822f6ada977f9685d21311b588/src/main/java/web/instaweb/WebConfig.java#L12-L30
  
</details>

<br/>

### 나의 작성 목록 

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/ecd1cc91-ed19-4701-8cbb-50d7dc7a68f1.png" width="20%" height="20%"/>

나의 작성 목록 탭에서는 내가 작성한 글들만 표시된다. 

<br/>

### Infinite Scroll 

**홈**, **나의 작성 목록** 에서 Page 목록이 보여질때는 스크롤을 내리면 서버에서 클라이언트로 새로운 Page 들을 ajax 로 보내준다. 

서버에서 Page 와 Page 에 속하는 첫 번째 이미지를 Wrap 해서 클라이언트로 보내주고, 클라이언트는 받아서 화면에 띄운다. 

<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/af3e3fab8dc91a822f6ada977f9685d21311b588/src/main/java/web/instaweb/controller/PageController.java#L159-L204
  
</details>


