# Instaweb

## 배포 주소 
https://lsh-instaweb.herokuapp.com/

## 도메인 모델 
![image](https://github.com/LSH3333/Instaweb/assets/62237852/e86889b7-e5c3-4cbc-bd51-3a214c8002c4)


## 주요 기능 
### 회원가입
### 로그인
로그인 여부 확인은 Spring Interceptor 통해서 진행.

모든 경로 막아 놓고 로그인 불필요한 경로(home 화면, 글 보기, ajax 요청 경로 등) 만 Interceptor 거치치 않도록 함
<details>
<summary>접기/펼치기</summary>

https://github.com/LSH3333/Instaweb/blob/3055332448adafbf4d22b2a890ca155be510a96b/src/main/java/web/instaweb/interceptor/LoginCheckInterceptor.java#L13-L37

https://github.com/LSH3333/Instaweb/blob/3055332448adafbf4d22b2a890ca155be510a96b/src/main/java/web/instaweb/WebConfig.java#L9-L40
  
</details>




