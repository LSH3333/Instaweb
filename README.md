
# 배포 주소 
(https://lsh-instaweb.herokuapp.com/)

# 도메인 모델 
<img src="https://github.com/LSH3333/Instaweb/assets/62237852/b1cd4c26-9f62-4473-9a16-f53f87c0ce3e.png" width="80%" height="80%"/>

# 주요 기능 

<br/>

<br/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/f1cd71b7-835f-492e-8dbd-af8f29cf835c.png" width="15px" height="15px"/> 가입

<img  alt="image" src="https://github.com/LSH3333/Instaweb/assets/62237852/a6391119-2a24-4b27-865e-25833bcd45dd" width="80%" height="80%" >

입력한 내용이 주어진 조건에 맞는지 확인, db에 중복되는 loginId, name 있는지 확인.

https://github.com/LSH3333/Instaweb/blob/main/src/main/java/web/instaweb/controller/MemberController.java

<br/><br/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/b1e18815-e808-4987-a3b9-459899261331.png" width="15px" height="15px"/>  로그인

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/ad5c3a10-c97f-482b-bb90-592edf075ee4.png" width="80%" height="80%"/>

로그인 여부 확인은 Spring Interceptor 통해서 진행.

모든 경로 막아 놓고 로그인 불필요한 경로(home 화면, 글 보기, ajax 요청 경로 등) 만 Interceptor 거치치 않도록 함
<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/3055332448adafbf4d22b2a890ca155be510a96b/src/main/java/web/instaweb/interceptor/LoginCheckInterceptor.java#L13-L37

https://github.com/LSH3333/Instaweb/blob/3055332448adafbf4d22b2a890ca155be510a96b/src/main/java/web/instaweb/WebConfig.java#L9-L40
  
</details>


<br/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/95b2c05c-bbfa-43f5-a53b-f25b37ca6852.png" width="15px" height="15px"/>  글 작성, 수정, 삭제  

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/c41861cd-402f-4485-9af9-679f5020c271.png" width="80%" height="80%"/>

글 작성, 수정에서 작성된 모든 내용 (제목,콘텐츠,이미지 등) 들은 모두 ajax 요청으로 서버로 보내서 저장 처리.

수정시 서버에서 해당 글에 저장된 모든것들 불러와서 다시 로드함.    

<br/><br/>
**이미지**는 사용자가 삽입했다가 지웠다가 할 수 있으므로 삽입시 html에 img 태그 삽입과 함께 고유 uuid 부여해서 해당 값을 key 값으로하는 map 에 저장해놓는다. 

이미지를 사용자가 지우면 img 태그가 지워진다.

글 작성이 끝나고 서버에 정보들이 보내질때 img 태그들과 map 을 대조해서 지워지지 않은 이미지들만 서버로 보낸다. 

**작성된 내용**들은 html 그대로 db 에 저장된다. 

<br/>

**에디터** 내 복사 붙여넣기 지원. (html element 를 통째로 복사해서 붙여 넣는 방법으로 속성(폰트크기 등)들 유지됨)

외부 프로그램에서 복사 붙여 넣기 시 모두 단순 텍스트로 변형해서 붙여넣어짐.


에디터 기능은 글 작성, 수정에서 동일하게 쓰이므로 thymeleaf fragment 로 만들어서 관리함.
(https://github.com/LSH3333/Instaweb/blob/main/src/main/resources/templates/fragment/editorData.html)

<br/>


## :x: 글 삭제 

로그인한 Member 가 작성한 Page 는 삭제할수 있다. 

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/f0c9e80e-d1f5-45c5-8c1d-9e8554096049.png" width="80%" height="80%"/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/cc5f42a0-796c-4292-96ba-656bc142725e.png" width="80%" height="80%"/>

스프링 인터셉터로 등록된 LoginCheckInterceptor 에서는 삭제를 포함한 모든 로그인이 필요한 요청에 대해, 로그인되지 않은 상태로 요청할 경우 로그인 화면으로 리다이렉트하도록 되어있다.

<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/a32aabc8b8631e3889c0daf8c77fc8c7de525973/src/main/java/web/instaweb/interceptor/LoginCheckInterceptor.java#L13-L37
  
</details>

<br/>
<br/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/24be8d3d-aee3-43a7-b3e8-190c81dc3694.png" width="15px" height="15px"/>  홈

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/ae2c64be-50d4-4aba-b9cc-14c971c24a8a.png" width="20%" height="20%"/>

홈에서는 모든 유저들이 작성한 글들을 볼 수 있다.

홈과 작성된 글들은 로그인 여부에 상관 없이 볼 수 있다. 

(스프링 인터셉터에서 LoginCheckInterceptor 의 검사 경로에서 제외)

<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/af3e3fab8dc91a822f6ada977f9685d21311b588/src/main/java/web/instaweb/WebConfig.java#L12-L30
  
</details>

<br/>


<img src="https://github.com/LSH3333/Instaweb/assets/62237852/aefd5535-1b90-4556-824c-3a636d558b2f.png" width="15px" height="15px"/>  나의 작성 목록 

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/ecd1cc91-ed19-4701-8cbb-50d7dc7a68f1.png" width="20%" height="20%"/>

나의 작성 목록 탭에서는 내가 작성한 글들만 표시된다. 

<br/>

:infinity: Infinite Scroll 

**홈**, **나의 작성 목록** 에서 Page 목록이 보여질때는 스크롤을 내리면 서버에서 클라이언트로 새로운 Page 들을 ajax 로 보내준다. 

서버에서 Page 들은 생성일 기준으로 정렬되어 저장되어 있고, 차례대로 클라이언트로 보내지기 때문에 최근에 작성된 Page 가 먼저 클라이언트로 보내진다. 

서버에서 Page 와 Page 에 속하는 첫 번째 이미지를 Wrap 해서 클라이언트로 보내주고, 클라이언트는 받아서 화면에 띄운다. 

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/d68a6b07-1122-4c7d-8bae-cd49dba4c056.png" width="80%" height="80%"/>

<details>
<summary>접기/펼치기</summary>

서버에서 데이터 wrap 해서 클라이언트로 보냄 
https://github.com/LSH3333/Instaweb/blob/af3e3fab8dc91a822f6ada977f9685d21311b588/src/main/java/web/instaweb/controller/PageController.java#L159-L204

클라이언트에서 받은 데이터를 토대로 화면에 보여줄수 있도록 html element 로 만듦. 
여러군데에서 사용하기 때문에 fragment 로 관리.
https://github.com/LSH3333/Instaweb/blob/main/src/main/resources/templates/fragment/wrapPageBlock.html

</details>


<br/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/b8b3e240-566a-4e56-bcaa-4a58c5b901b2.png" width="15px" height="15px"/>  글 검색 

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/7e0321d5-f1f8-447e-9ec2-bc362df8a2d7.png" width="20%" height="20%"/>

글 검색은 db 에서 저장된 Page 들을 가져와서 title, content에 유저가 검색한 searchQuery 문자와 일치하는 문자가 있는지 탐색한다.

content 는 html 통째로 저장되기 때문에 단순히 searchQuery 와 일치하는 문자열이 존재하는지 탐색하면 element 와 충돌하는 경우가 있는데 ("a" 검색시 <a> 태그를 찾음), 

해결하기 위해 element 들을 모두 골라내 text 부분만을 대상으로 searchQuery 와 대조해본다.

<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/4bc9a2228f3c4b98ee0f3d970ff2c20d8d33e25e/src/main/java/web/instaweb/repository/PageRepository.java#L119-L143

</details>

<br/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/31cd3047-05dc-4e97-8697-5912fc2922aa.png" width="80%" height="80%"/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/ef4a46f0-2077-4058-ae13-9ecdecb1f2ed.png" width="80%" height="80%"/>

<br/>
<br/>

:speech_balloon: 댓글 

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/3d439281-c9ac-4f8b-8678-1795bb8d329b.png" width="80%" height="80%"/>

댓글 입력, 삭제할때는 ajax 로 동적으로 처리. 

변경 발생할때마다 클라이언트의 댓글 모두 지우고, 서버에서 통째로 가져와서 다시 디스플레이하는 방식. 

<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/main/src/main/resources/templates/pages/pageView.html#L73-L206

https://github.com/LSH3333/Instaweb/blob/9af57f52e3ccb1d8a411621c36bc7e298c2f1e1f/src/main/resources/templates/pages/pageView.html#L73-L206

</details>

<br/>
<br/>

:iphone:	모바일 ui 지원 

<img alt="image" src="https://github.com/LSH3333/Instaweb/assets/62237852/64769c8d-8705-47e9-b32b-a9ceda56cfe8" width="40%" height="40%">

<img alt="image" src="https://github.com/LSH3333/Instaweb/assets/62237852/e7b9fbbf-f8df-4b07-8bd1-b5f8c3e7846d" width="40%" height="40%">

좌: 데스크톱 웹 

우: 모바일 

css @media 로 화면 크기 감지해서 html element 변경으로 처리. 

https://github.com/LSH3333/Instaweb/blob/main/src/main/resources/static/css/pageBlock.css#L106-L155


<br/>
<br/>


<img src="https://github.com/LSH3333/Instaweb/assets/62237852/fdbfeb82-f933-4b2a-a59d-e1cd7b585861.png" width="15px" height="15px"/>  게임 

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/8d410c4f-8bee-46de-b028-7c3cefe6ba62.png" width="20%" height="20%"/>

<img src="https://github.com/LSH3333/Instaweb/assets/62237852/6a7e1766-b09d-4de9-9e04-5160ba59d101.png" width="80%" height="80%"/>

Unity 로 만든 게임들을 itch.io 라는 인디게임들을 올릴수 있는 사이트에 올려 놓았는데 링크를 타고 가서 게임을 플레이해볼수 있다. 

Galaga git

https://github.com/LSH3333/Galaga

ManyGames git 

https://github.com/LSH3333/Unity_ManyGames

