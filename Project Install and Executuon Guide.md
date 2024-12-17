# Project Install and Execution Guide

0. **테스트 환경**
   - 필요 Java version: 21 이상
   - file directory structor ↓

바탕화면/  
&nbsp;&nbsp;├── java/    
&nbsp;&nbsp;│&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── team-project-27/    
&nbsp;&nbsp;│&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── target/  
&nbsp;&nbsp;│&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;│&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── scheduler-alert-app-start2024116.jar  
&nbsp;&nbsp;│&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;│&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── ...  
&nbsp;&nbsp;│&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── ...  
&nbsp;&nbsp;│  
&nbsp;&nbsp;└── project_Scheduler/  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── project_Scheduler.exe  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── project_Scheduler.xml  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── autoLoginData.json (해당 파일은 존재하지 않아도 exe 실행 시 자동 생성됩니다.)

1. **zip 파일**
   - .jar zip파일의 압축해제된 file은 team-project-27/target 안에 있어야 합니다.

2. **serviceAccountKey.json**
   - 일반적인 배포가 불가능하기에 해당 파일의 data를 메일로 보내드린 data로 덮어쓰기를 해주셔야 합니다.
   - 유효한 serviceAccountKey.json을 Github에 push하면 바로 만료가 되어, 부득이하게 메일로 보내는 점에 대해 양해부탁드립니다.
3. **무한로딩**
   - 프로그램이 무한 로딩에 걸려서 종료키가 먹통이 되는 경우, 현재까지 확인된 바로는 serviceAccountKey가 만료되어 나타난 버그로 새로운 serviceAccountKey를 할당받은 후 해당 json파일으로 덮어쓰기를 해야지 해결됩니다.
   - 프로그램 종료는 작업관리자에서 강제 종료를 해야합니다. 
