package schedulerAlertApp;
//Firebase library
import com.google.firebase.database.DatabaseReference;  
import com.google.firebase.database.FirebaseDatabase;
import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener; 
//Jackson library
import com.fasterxml.jackson.databind.ObjectMapper; 
//JBCrypt library
import org.mindrot.jbcrypt.BCrypt;  

import org.checkerframework.checker.units.qual.g;
import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.t;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Register {
    private String userId;  //if login successed, store userId. if logout, set userId to null.
    private DatabaseReference ref;  //Firebase database reference(path)
    private static final int GENSALTNUM = 12;   //BCrypt.gensalt() number
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; //random string generator source

    public Register(){
        System.out.println("Register object created start");    //This comment is for debugging
        this.userId = null;
        try {
            FirebaseInit.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Check if "autoLoginData.json" exists
        // if "autoLoginData.json" does not exist, create it with default data
        try {
            String fileName = "autoLoginData.json";
            File file = new File(fileName);
            if (!file.exists()) {
                ObjectMapper objectMapper = new ObjectMapper();
                AutoLoginData autoLoginData = new AutoLoginData("test", "test", "test");
                objectMapper.writeValue(file, autoLoginData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Register object created end");  //This comment is for debugging
    }

    protected CompletableFuture<Boolean> register(String id, String password, int questionIndex, String questionAns){  //debugging complete
        // Refer to the "users" path
        ref = FirebaseDatabase.getInstance().getReference("users");
        // Organize user register data
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        boolean isSuccessful = false;
        Map<String, Object> userData = new HashMap<>(); 
        userData.put("password", BCrypt.hashpw(password, BCrypt.gensalt(GENSALTNUM)));
        userData.put("questionIndex", questionIndex);
        userData.put("questionAns", questionAns);
        userData.put("autoLoginStr", "NULL");
        userData.put("autoLoginKey", "NULL");
        // Set userData
        ApiFuture<Void> apiFuture = ref.child(id).setValueAsync(userData);
        toCompletableFuture(apiFuture).thenAccept(aVoid -> {
            System.out.println("userData successfully created.");
            future.complete(true);
        }).exceptionally(e -> {
            System.err.println("Failed to create userData: " + e.getMessage());
            future.complete(false);
            return null;
        });

        return future;
    }

    protected int login(String id, String password, boolean autoLogin){ //debugging complete
        System.out.println("Login Start");  //This comment is for debugging
        //Check id
        List<String> userIdList=new ArrayList<>();
        CompletableFuture<String[]> futureId = getKeyArray(false);
        futureId.thenAccept(keys -> {
            System.out.println("Keys class:" + keys.getClass().getName());  //This comment is for debugging
        }).exceptionally(e -> {
            System.err.println("Error getting keys: " + e.getMessage());
            return null;
        });
        try {
            userIdList=Arrays.asList(futureId.get()); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if (userIdList.isEmpty()) 
            return 0;
        if(!userIdList.contains(id))
            return 2;
        
        System.out.println("Id checked");   //This comment is for debugging
        //Check password 
        CompletableFuture<String> futureS = getData("/" + id + "/password",String.class);
        String storedPassword;
        futureS.thenRun(() -> {
            System.out.println("Password updated successfully.");   //This comment is for debugging
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return null;
        });
        try {
            storedPassword=futureS.get(); // 비동기 작업이 끝날 때까지 대기
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if(!BCrypt.checkpw(password, storedPassword))
            return 3;
        userId = id;    //Login successed
        System.out.println("Password checked"); //This comment is for debugging
        //Check autoLogin
        if(autoLogin){
            //Create autoLoginStr and autoLoginKey
            SecureRandom random = new SecureRandom();
            String randomStr = IntStream.range(0, 8) 
                    .mapToObj(i -> String.valueOf(CHARSET.charAt(random.nextInt(CHARSET.length()))))
                    .collect(Collectors.joining());
            String randomKey = IntStream.range(0, 8)
                    .mapToObj(i -> String.valueOf(CHARSET.charAt(random.nextInt(CHARSET.length()))))
                    .collect(Collectors.joining());
            String hashStr = BCrypt.hashpw(randomStr, BCrypt.gensalt(GENSALTNUM));
            String hashKey = BCrypt.hashpw(randomKey, BCrypt.gensalt(GENSALTNUM));
            //Set autoLoginStr and autoLoginKey to Firebase
            boolean isSuccessful = false;
            CompletableFuture<Boolean> FutureAuto1 = setData("/autoLoginStr", hashStr);
            FutureAuto1.thenApply(result -> {
                return true;
            }).exceptionally(e -> {
                System.err.println("Error setting password: " + e.getMessage());
                return false;
            });
            CompletableFuture<Boolean> FutureAuto2 =setData("/autoLoginKey", randomKey);
            FutureAuto2.thenApply(result -> {
                return true;
            }).exceptionally(e -> {
                System.err.println("Error setting password: " + e.getMessage());
                return false;
            });
            try {
                if(FutureAuto1.get()&&FutureAuto2.get()) // wait for the asynchronous task to complete
                    isSuccessful = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!isSuccessful)
                return 4;
            else{
                try{
                    ObjectMapper objectMapper = new ObjectMapper();
                    AutoLoginData autoLoginData = new AutoLoginData(id, randomStr, hashKey);
                    objectMapper.writeValue(new File("autoLoginData.json"), autoLoginData);
                }catch(Exception e){
                    e.printStackTrace();
                    return 4;
                }
            }
        }
        return 1;
    }

    protected CompletableFuture<Boolean> logout(){  //debugging complete
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture<Boolean> future1 = setData("/autoLoginStr", "NULL");
        CompletableFuture<Boolean> future2 = setData("/autoLoginKey", "NULL");
        future1.thenApply(result -> {
            return true;
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return false;
        });
        future2.thenApply(result -> {
            return true;
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return false;
        });
        try {
            if(future1.get() && future2.get()){ // wait for the asynchronous task to complete
                userId = null;
                ref = FirebaseDatabase.getInstance().getReference("users");
                future.complete(true);
            }
            else
                future.complete(false);
        } catch (Exception e) {
            e.printStackTrace();
            future.complete(false);
        }
        return future;
    }

    protected boolean autoLogin(){  //debugging complete
        ObjectMapper objectMapper = new ObjectMapper();
        AutoLoginData autoLoginData;
        try {
            autoLoginData = objectMapper.readValue(new File("autoLoginData.json"), AutoLoginData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }        
        //Check autoLoginData
        //1. Check id
        List<String> userIdList=new ArrayList<>();
        CompletableFuture<String[]> futureId = getKeyArray(false);
        futureId.thenAccept(keys -> {
            System.out.println("Keys class:" + keys.getClass().getName());  //This comment is for debugging
        }).exceptionally(e -> {
            System.err.println("Error getting keys: " + e.getMessage());
            return null;
        });
        try {
            userIdList=Arrays.asList(futureId.get()); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if(!userIdList.contains(autoLoginData.getId()))
            return false;
        String id = autoLoginData.getId();
        //2. Check autoLoginStr and autoLoginKey
        String storedStrFB;
        String storedKeyFB;
        CompletableFuture<String> futureS1 = getData("/" + id + "/autoLoginStr",String.class);
        CompletableFuture<String> futureS2 = getData("/" + id + "/autoLoginKey",String.class);
        futureS1.thenRun(() -> {
            System.out.println("Password updated successfully.");   //This comment is for debugging
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return null;
        });
        futureS2.thenRun(() -> {
            System.out.println("Password updated successfully.");   //This comment is for debugging
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return null;
        });
        try {
            storedStrFB=futureS1.get(); // wait for the asynchronous task to complete
            storedKeyFB=futureS2.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        if(BCrypt.checkpw(autoLoginData.getStr(), storedStrFB) && BCrypt.checkpw(storedKeyFB,autoLoginData.getKey())){   //autoLogin successed
            userId = id;
            System.out.println("AutoLogin successed: userId = " + userId);  //This comment is for debugging
            return true;
        }
        return false;
    }

    protected int findPassword(String id, int questionIndex, String questionAns, String newPassword){   //debugging complete
        // check id
        List<String> userIdList=new ArrayList<>();
        CompletableFuture<String[]> futureId = getKeyArray(false);
        futureId.thenAccept(keys -> {
            System.out.println("Keys class:" + keys.getClass().getName());  //This comment is for debugging
        }).exceptionally(e -> {
            System.err.println("Error getting keys: " + e.getMessage());
            return null;
        });
        try {
            userIdList=Arrays.asList(futureId.get()); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if (userIdList.isEmpty()) 
            return 0;
        if(!userIdList.contains(id))
            return 2;
        
        System.out.println("Id checked");   //This comment is for debugging
        // check question
        CompletableFuture<Long> futureI = getData("/" + id + "/questionIndex",Long.class);
        futureI.exceptionally(ex -> {
            System.err.println("Failed to read data: " + ex.getMessage());  //This comment is for debugging
            return Long.valueOf(-1);
        }).thenAccept(result -> {});
        int storedQuestionIndex=-1;
        try{
            storedQuestionIndex=futureI.get().intValue(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        CompletableFuture<String> futureS=getData("/" + id + "/questionAns",String.class);
        futureS.thenAccept(result -> {})
            .exceptionally(ex -> {
                System.err.println("Failed to read data: " + ex.getMessage());  //This comment is for debugging
                return null;
            });
        String storedQuestionAns=null;
        try {
            storedQuestionAns= futureS.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if (storedQuestionIndex == -1 || storedQuestionAns.equals(null)) 
            return 0;   //other error
        else if(storedQuestionIndex != questionIndex || !storedQuestionAns.equals(questionAns))
            return 3;   //wrong question answer
        // change password
        
        CompletableFuture<Boolean> futureB=setData("/" + id + "/password", BCrypt.hashpw(newPassword, BCrypt.gensalt(GENSALTNUM)));
        futureB.thenApply(result -> {
            return true;
            }).exceptionally(ex -> {
                System.err.println("Failed to write data: " + ex.getMessage());
                return false;
            });
        boolean isSuccessful=false;
        try{
            isSuccessful = futureB.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if (isSuccessful) 
            return 1;
        return 0;
    }

    protected CompletableFuture<String[]> getKeyArray(boolean isSchedules){ //debugging complete
        CompletableFuture<String[]> future = new CompletableFuture<>();
        ref = FirebaseDatabase.getInstance().getReference("users");
        if(isSchedules == true && !userId.equals(null))
            ref = ref.child(userId).child("schedules");
        List<String> keys = new ArrayList<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 모든 키 출력
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    keys.add(child.getKey());
                }
                future.complete(keys.toArray(new String[0]));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error: " + databaseError.getMessage());
                future.completeExceptionally(new Exception(databaseError.getMessage()));
            }
        });
        
        return future;
    }

    protected <T> CompletableFuture<T> getData(String path, Class<T> type){ //debugging complete
        CompletableFuture<T> returnFuture = new CompletableFuture<>();
        ref = FirebaseDatabase.getInstance().getReference("users");
        if(userId != null)
            ref = ref.child(userId);
        String[] keyParts = path.split("/");
        for(String key : keyParts){
            ref = ref.child(key);
        }
        
        CompletableFuture<DataSnapshot> dataFuture = getWorkData();
        dataFuture.thenAccept(snapshot -> {
            if (snapshot.exists()){
                Object value;
                if(type.equals(Long.class))
                    value = snapshot.getValue(Long.class);
                else{
                    value = snapshot.getValue(String.class);
                    if(type.equals(List.class))
                        value = stringToList((String) value);
                }
                returnFuture.complete((T) value);
            }else{
                System.out.println("No data found at the specified path."); //This comment is for debugging
                returnFuture.complete(null);
            }
        }).exceptionally(e -> {
            System.err.println("Error reading data: " + e.getMessage());
            returnFuture.completeExceptionally(e);
            return null;
        });
        try {
            dataFuture.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnFuture;
    }
    
    private CompletableFuture<DataSnapshot> getWorkData(){ //debugging complete
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot);
            }
    
            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new Exception("Firebase Error: " + error.getMessage()));
            }
        });
        return future;
    }

    protected CompletableFuture<Boolean> setData(String path, String data){ //debugging complete
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        boolean addSchedule = false;
        ref = FirebaseDatabase.getInstance().getReference("users");
        if(userId != null)
            ref = ref.child(userId);
        String[] keyParts = path.split("/");
        for(String key : keyParts){
            if(key.equals("schedules"))
                addSchedule = true;
            ref = ref.child(key);
        }

        if(addSchedule){
            CompletableFuture<DataSnapshot> dataFuture = getWorkData();
            String listToString = null;
            List<String> list = new ArrayList<>();
            CompletableFuture<String> stringFuture = dataFuture.thenApply(snapshot -> {
                if (snapshot.exists()) {
                    return snapshot.getValue(String.class); 
                } else {
                    System.err.println("No data found in snapshot");
                    return null;
                }
            });
            try {
                DataSnapshot snapshot = dataFuture.join(); // wait for the asynchronous task to complete
                if (snapshot.exists()) {
                    listToString = snapshot.getValue(String.class); // Convert DataSnapshot to String
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            CompletableFuture<Boolean> setFuture;

            if(listToString != null){
                list = stringToList(listToString);
                list.add(data);
                setFuture = setWorkData(list.toString());
                setFuture.thenApply(result -> {
                    future.complete(true);
                    return true;
                }).exceptionally(e -> {
                    System.err.println("Error setting password: " + e.getMessage());
                    future.complete(false);
                    return false;
                });
                try {
                    setFuture.get(); // wait for the asynchronous task to complete
                } catch (Exception e) {
                    e.printStackTrace();
                    future.complete(false);
                }
            }
            else{
                list.add(data);
                setFuture = setWorkData(list.toString());
                setFuture.thenApply(result -> {
                    future.complete(true);
                    return true;
                }).exceptionally(e -> {
                    System.err.println("Error setting password: " + e.getMessage());
                    future.complete(false);
                    return false;
                });
                try {
                    setFuture.get(); // wait for the asynchronous task to complete
                } catch (Exception e) {
                    e.printStackTrace();
                    future.complete(false);
                }
            }
        } else {
            CompletableFuture<Boolean> setFuture = setWorkData(data);
            setFuture.thenApply(result -> {
                future.complete(true);
                return true;
            }).exceptionally(e -> {
                System.err.println("Error setting password: " + e.getMessage());
                future.complete(false);
                return false;
            });
            try {
                setFuture.get(); // wait for the asynchronous task to complete
            } catch (Exception e) {
                e.printStackTrace();
                future.complete(false);
            }
        }
        return future;
    }

    private CompletableFuture<Boolean> setWorkData(String data){ //debugging complete
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        ref.setValue(data, (error, ref) -> {
            if (error == null) {
                future.complete(true); 
            } else {
                System.err.println("Error setting data: " + error.getMessage());
                future.completeExceptionally(new Exception("Firebase Error: " + error.getMessage()));
            }
        });

        return future;
    }

    protected CompletableFuture<Boolean> delData(String path,int index){    //debugging complete
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        boolean isExistTestData = true;
        boolean isSchedules = false;
        ref = FirebaseDatabase.getInstance().getReference("users");
        String[] keyParts = path.split("/");
        for(String key : keyParts){
            if(!key.equals("test")) // always need for testData
                isExistTestData = false;
            if(key.equals("schedules"))
                isSchedules = true;
            ref = ref.child(key);
        }
        if(!isExistTestData){   // if testData is not exist, create testData. if failed, return false now.
            CompletableFuture<Boolean> futureB= setData("default", "This is default data.");
            futureB.thenApply(result -> {
                return true;
                }).exceptionally(ex -> {
                    System.err.println("Failed to create test data: " + ex.getMessage());
                    return false;
                });
            try {
                futureB.get(); // wait for the asynchronous task to complete
            } catch (Exception e) {
                e.printStackTrace();
                future.complete(false);
                return future;
            }
        }

        CompletableFuture<List> getFuture;
        List<String> testArray = new ArrayList<>();
        if(isSchedules){
            getFuture = getData(path, List.class);
            getFuture.thenRun(() -> {
                System.out.println("Password updated successfully.");   //This comment is for debugging
            }).exceptionally(e -> {
                System.err.println("Error setting password: " + e.getMessage());
                return null;
            });
            try {
                testArray=getFuture.get(); // wait for the asynchronous task to complete
            } catch (Exception e) {
                e.printStackTrace();
                future.complete(false);
            }
        }

        CompletableFuture<Boolean> delFuture = delWorkData();
        delFuture.thenApply(result -> {
            future.complete(true);
            return true;
        }).exceptionally(e -> {
            System.err.println("Error deleting data: " + e.getMessage());
            future.complete(false);
            return false;
        });
        try {
            delFuture.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
            future.complete(false);
        }
        //midterm inspection
        boolean middleSuccess = true;
        try {
            middleSuccess = future.get();
        } catch (Exception e) {
            e.printStackTrace();
            middleSuccess = false;
        }
        if(middleSuccess == false)
            return future;
        //Delete specific index
        if(isSchedules && index != -1){
            if (index >= 0 && index < testArray.size()) {
                testArray.remove(index);
            } else {
                System.err.println("Invalid index: " + index + ". List size: " + testArray.size());
                future.complete(false);
                return future;
            }
            CompletableFuture<Boolean> setFuture = setWorkData(testArray.toString());
            setFuture.thenApply(result -> {
                future.complete(true);
                return true;
            }).exceptionally(e -> {
                System.err.println("Error setting password: " + e.getMessage());
                future.complete(false);
                return false;
            });
            try {
                setFuture.get(); // wait for the asynchronous task to complete
            } catch (Exception e) {
                e.printStackTrace();
                future.complete(false);
            }
        }

        return future;
    }

    private CompletableFuture<Boolean> delWorkData(){ //debugging complete
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        ref.removeValue((databaseError, databaseReference) -> {
            if (databaseError == null) {
                System.out.println("Data deleted successfully!");   //This comment is for debugging
                future.complete(true);
            } else {
                System.err.println("Error deleting data: " + databaseError.getMessage());
                future.complete(false);
            }
        });
        try{
            future.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
        }
        return future;
    }

    protected String getUserId(){ 
        return userId;
    }

    private static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {   //used in register()
        CompletableFuture<T> future = new CompletableFuture<>();
        apiFuture.addListener(() -> {
            try {
                future.complete(apiFuture.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, Runnable::run);
        return future;
    }

    private static class AutoLoginData {   //used in autoLogin()
        private final String id;
        private final String str;
        private final String key;

        public AutoLoginData() {
            this.id = null;
            this.str = null;
            this.key = null;
        }

        public AutoLoginData(String id, String str, String key) {
            this.id = id;
            this.str = str;
            this.key = key;
        }
        public String getId() {
            return id;
        }
        public String getStr() {
            return str;
        }
        public String getKey() {
            return key;
        }
    }

    public static List stringToList(String listAsString) { //change string to list
        // Remove square brackets
        String trimmedString = listAsString.substring(1, listAsString.length() - 1);
    
        // Handle empty case
        if (trimmedString.isEmpty()) {
            return new ArrayList<>();
        }
    
        // Split by ", " and return as List
        return new ArrayList<>(Arrays.asList(trimmedString.split(", ")));
    }

    protected void printRef(){  //This function is for debugging
        System.out.println(ref.toString());
    }
    public static void main(String[] args) {
        System.out.println("Register Test Start");
        Register registers = new Register();
        System.out.println("Register Test End");
    }
}
/*
//--------------------------protect Function usage--------------------------//
1. 회원가입 함수
    #CompletableFuture<Boolean> register(String id, String password, int questionIndex, String questionAns)
    - id, password, questionIndex, questionAns를 입력받아 새로운 사용자 등록을 시도합니다.
    - 추가로 autoLoginStr, autoLoginKey를 "NULL"(String)로 초기화합니다.
    - 등록이 성공하면 true, 실패하면 false를 반환하는 CompletableFuture<Boolean> 객체를 반환합니다.
    - password는 BCrypt를 사용하여 암호화합니다.
    - 사용 예시
        CompletableFuture<Boolean> future = register("testid", "testpassword", 1, "testAns");
        future.thenApply(result -> {
            System.out.println("Password updated successfully.");
            return true;
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return false;
        });
        try {
            future.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
        }

2. 로그인 함수
    #int login(String id, String password, boolean autoLogin)
    - id, password, autoLogin을 입력받아 로그인을 시도합니다.
    - autoLogin이 true일 경우 login을 성공한 id와 autoLoginStr, autoLoginKey(암호화)를 생성하여 컴퓨터의 
      autoLoginData.json을 생성 및 저장하고 Firebase에는 autoLoginStr(암호화), autoLoginKey를 저장합니다.
    - 로그인에 Error가 발생하면 0을 반환합니다.
    - 로그인 성공 시 userId에 id를 저장하고 1을 반환합니다.
    - 입력한 id가 존재하지 않을 경우 2를 반환합니다.
    - 비밀번호가 틀렸을 경우 3을 반환합니다.
    - autoLogin을 시도하다가 실패후 Login만 성공할 시 4를 반환합니다.
    - 사용 예시
        int result;
        result=registers.login("plzLastTest", "holymoly", true);
        System.out.println("Login result: " + result);
        System.out.println("UserId: " + registers.getUserId());
        registers.printRef();

3. 로그아웃 함수
    #CompletableFuture<Boolean> logout()
    - 로그아웃을 시도합니다.
    - autoLoginStr, autoLoginKey를 "NULL"(String)로 초기화합니다.
    - 로그아웃 성공 시 true, 실패 시 false를 반환하는 CompletableFuture<Boolean> 객체를 반환합니다.
    - 사용 예시
        Boolean result;
        result = registers.logout().join();
        System.out.println("Logout result: " + resultB);
        System.out.println("UserId: " + registers.getUserId());
        registers.printRef();

4. 자동 로그인 함수
    #boolean autoLogin()
    - autoLoginData.json을 읽어 Firebase에 저장된 autoLoginStr, autoLoginKey를 비교하여 자동 로그인을 시도합니다.
    - 자동 로그인 성공 시 true, 실패 시 false를 반환합니다.
    - 사용 예시
        boolean result;
        result=registers.autoLogin();
        System.out.println("AutoLogin result: " + result);

5. 비밀번호 찾기 함수
    #int findPassword(String id, int questionIndex, String questionAns, String newPassword)
    - id, questionIndex, questionAns, newPassword를 입력받아 비밀번호 찾기를 시도합니다.
    - 비밀번호 찾기에 Error가 발생하면 0을 반환합니다.
    - 비밀번호 변경에 성공하면 1을 반환합니다.
    - id가 존재하지 않을 경우 2를 반환합니다.
    - questionIndex, questionAns가 일치하지 않을 경우 3을 반환합니다.
    
    - 사용 예시
        int result;
        result=registers.findPassword("testid1", 1, "testAns1", "testnewpassword");
        System.out.println("FindPassword result: " + result);

6. 키 배열 가져오기 함수
    #CompletableFuture<String[]> getKeyArray(boolean isSchedules)
    - isSchedules가 true일 경우 등록된 userId의 schedules의 키 배열(일정이 저장된 날짜)을 가져옵니다. (ex.날짜: 20241231)
    - isSchedules가 false일 경우 users의 키 배열(등록된 사용자의 Id)을 가져옵니다.
    - 키 배열을 가져오는데 성공하면 String[]을 반환하는 CompletableFuture<String[]> 객체를 반환합니다.
    - 실패 시 toArray(new String[0]) 또는 new Exception(databaseError.getMessage())을 반환합니다.
    - 사용 예시
        List<String> userIdList=new ArrayList<>();
        CompletableFuture<String[]> futureId = registers.getKeyArray(false);
        futureId.thenAccept(keys -> {
            for (String key : keys) {
                System.out.println("Key: " + key); 
            }
        }).exceptionally(e -> {
            System.err.println("Error getting keys: " + e.getMessage());
            return null;
        });
        try {
            userIdList=Arrays.asList(futureId.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("userIdList: " + userIdList);

7. 데이터 가져오기 함수
    #<T> CompletableFuture<T> getData(String path, Class<T> type)
    - path와 type을 입력받아 Firebase에 저장된 데이터를 가져옵니다. (type으로는 String, Long, List만 사용합니다.)
    - path 형식은 "users/testid/password"와 같이 "/"로 구분합니다.
    - 가져온 데이터를 type에 맞게 변환하여 반환하는 CompletableFuture<T> 객체를 반환합니다.
    - 사용 예시(String)
        CompletableFuture<String> getFuture = registers.getData("testid/password", String.class);
        String result=new String();
        getFuture.thenRun(() -> {
            System.out.println("Password updated successfully.");
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return null;
        });
        try {
            result=getFuture.get(); // 비동기 작업이 끝날 때까지 대기
        } catch (Exception e) {
            e.printStackTrace();
        }
    - 사용 예시(List)
        CompletableFuture<List> getFuture = registers.getData("plzLastTest/schedules/20241128", List.class);
        List<String> testArray = new ArrayList<>();
        getFuture.thenRun(() -> {
            System.out.println("Password updated successfully.");
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return null;
        });
        try {
            testArray=getFuture.get(); // 비동기 작업이 끝날 때까지 대기
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Data retrieved: " + testArray);
    - 사용 예시(Long)
        CompletableFuture<Long> getFuture = registers.getData("testid/questionIndex", Long.class);
        Long result=new Long(0);
        getFuture.thenRun(() -> {
            System.out.println("Password updated successfully.");
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return null;
        });
        try{
            result=getFuture.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Data retrieved: " + result);

8. 데이터 저장 함수
    #CompletableFuture<Boolean> setData(String path, String data)
    - path와 data(String)를 입력받아 Firebase에 데이터를 저장합니다.
    - path에 "schedules"가 포함되어 있으면 data를 List로 변환하여 기존 Data에 추가하여 저장합니다.
    - path에 "schedules"가 포함되어 있지 않으면 기존 data를 덮어쓰는 방식으로 저장합니다.
    - 저장에 성공하면 true, 실패하면 false를 반환하는 CompletableFuture<Boolean> 객체를 반환합니다.
    - 사용 예시
        CompletableFuture<Boolean> setFuture = registers.setData("plzLastTest/schedules/20241128", "testing3");
        setFuture.thenApply(result -> {
            return true;
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return false;
        });
        try {
            setFuture.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
        }

9. 데이터 삭제 함수
    #CompletableFuture<Boolean> delData(String path,int index)
    - path와 index를 입력받아 Firebase에 저장된 데이터를 삭제합니다.
    - index가 -1이면 path에 해당하는 데이터를 전부 삭제합니다.
    - 만약 path에 "schedules"가 포함되어 있으며 index가 -1이 아닐 경우, index에 해당하는 데이터를 삭제합니다.
    - 삭제에 성공하면 true, 실패하면 false를 반환하는 CompletableFuture<Boolean> 객체를 반환합니다.
    - 사용 예시
        CompletableFuture<Boolean> delFuture = registers.delData("abcd/schedules/20241127", 2);
        delFuture.thenApply(result -> {
            System.out.println("Password updated successfully.");
            return true;
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return false;
        });
        try {
            delFuture.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
        }

10. 사용자 ID 가져오기 함수
    #String getUserId()
    - userId를 반환합니다.
    - 사용 예시
        System.out.println("UserId: " + registers.getUserId());

11. 문자열 리스트 변환 정적 함수
    #public static List stringToList(String listAsString)
    - 문자열 배열을 List로 변환합니다.
    - List형태로 저장된 Data를 다루기 위해 필요한 정적 메소드입니다.
    - 사용 예시
        List<String> testArray = new ArrayList<>();
        testArray = stringToList("[\"test1\", \"test2\", \"test3\"]");
        System.out.println("Data retrieved: " + testArray);

*/
