package schedulerAlertApp;
//package io.github.classroomorg.team27; //test용

import com.google.firebase.database.DatabaseReference;  //Firebase library
import com.google.firebase.database.FirebaseDatabase;
import com.google.api.core.ApiFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener; 
import com.fasterxml.jackson.databind.ObjectMapper; //Jackson library

import org.checkerframework.checker.units.qual.g;
import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.t;
import org.mindrot.jbcrypt.BCrypt;  //JBCrypt library

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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Register { //complete
    protected String userId;
    private DatabaseReference ref;
    private static final int GENSALTNUM = 12;
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public Register(){  //complete
        System.out.println("Register object created start");
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
    }

    private static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
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

    protected CompletableFuture<Boolean> register(String id, String password, int questionIndex, String questionAns){  //complete
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
        userData.put("schedules", new HashMap<>());
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

    protected CompletableFuture<String[]> getKeyArray(boolean isSchedules){ //complete
        CompletableFuture<String[]> future = new CompletableFuture<>();
        ref = FirebaseDatabase.getInstance().getReference("users");
        if(isSchedules == true && userId != null)
            ref.child(userId).child("schedules");
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

    protected int login(String id, String password, boolean autoLogin){ //complete
        //Check id
        ref = FirebaseDatabase.getInstance().getReference("users");
        CompletableFuture<List<String>> futureLS = getKeyArray(false)
            .thenApply(userIdsList -> Arrays.asList(userIdsList)) 
            .exceptionally(ex -> {
                System.err.println("Failed to retrieve userIdsList: " + ex.getMessage());
                return Collections.emptyList(); 
            });

        List<String> userIdList = futureLS.join(); 
        if (userIdList.isEmpty()) 
            return 0;
        if(!userIdList.contains(id))
            return 2;

        //Check password 
        
        CompletableFuture<String> futureS = getData("/" + id + "/password",String.class)
            .exceptionally(ex -> {
                System.out.println("Failed to read data: " + ex.getMessage());
                return null;
            });
        String storedPassword = futureS.join(); 

        if(!storedPassword.equals(password))
            return 3;

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
            CompletableFuture<Boolean> futureB = setData("/" + id + "/autoLoginStr", hashStr)
                .exceptionally(ex -> {
                    System.out.println("Failed to write data: " + ex.getMessage());
                    return false;
                });
            boolean isSuccessful = futureB.join();
            if(!isSuccessful)
                return 4;
            futureB = setData("/" + id + "/autoLoginKey", randomKey)
                .exceptionally(ex -> {
                    System.out.println("Failed to write data: " + ex.getMessage());
                    return false;
                });
            isSuccessful = futureB.join();
            //Successed autoLogin
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

    protected boolean autoLogin(){  //complete
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
        ref = FirebaseDatabase.getInstance().getReference("users");
        CompletableFuture<List<String>> futureLS = getKeyArray(false)
            .thenApply(userIdsList -> Arrays.asList(userIdsList)) 
            .exceptionally(ex -> {
                System.err.println("Failed to retrieve userIdsList: " + ex.getMessage());
                return Collections.emptyList(); 
            });
        List<String> userIdList = futureLS.join();
        if(!userIdList.contains(autoLoginData.getId()))
            return false;
        String id = autoLoginData.getId();
        //2. Check autoLoginStr and autoLoginKey
        CompletableFuture<String> futureS=getData("/" + id + "/autoLoginStr",String.class)
            .exceptionally(ex -> {
                System.out.println("Failed to read data: " + ex.getMessage());
                return null;
            });
        String storedStrFB = futureS.join();
        futureS=getData("/" + id + "/autoLoginKey",String.class)
            .exceptionally(ex -> {
                System.out.println("Failed to read data: " + ex.getMessage());
                return null;
            });
        String storedKeyFB = futureS.join();
        if(BCrypt.checkpw(autoLoginData.getStr(), storedStrFB) && BCrypt.checkpw(storedKeyFB,autoLoginData.getKey())){   //autoLogin successed
            userId = id;
            return true;
        }
        return false;
    }

    protected int findPassword(String id, int questionIndex, String questionAns, String newPassword){   //complete
        // check id
        CompletableFuture<List<String>> futureLS = getKeyArray(false)
            .thenApply(userIdsList -> Arrays.asList(userIdsList)) 
            .exceptionally(ex -> {
                System.err.println("Failed to retrieve userIdsList: " + ex.getMessage());
                return Collections.emptyList(); 
            });
        List<String> userIdList = futureLS.join(); 
        if (userIdList.isEmpty()) 
            return 0;
        if(!userIdList.contains(id))
            return 2;

        // check question
        CompletableFuture<Integer> futureI = getData("/" + id + "/questionIndex",Integer.class)
            .exceptionally(ex -> {
                System.out.println("Failed to read data: " + ex.getMessage());
                return -1;
            });
        int storedQuestionIndex = futureI.join();
        CompletableFuture<String> futureS=getData("/" + id + "/questionAns",String.class)
            .exceptionally(ex -> {
                System.out.println("Failed to read data: " + ex.getMessage());
                return null;
            });
        String storedQuestionAns = futureS.join();
        if (storedQuestionIndex == -1 || storedQuestionAns == null) 
            return 0;
        // change password
        if(storedQuestionAns.equals(questionAns)&&storedQuestionIndex==questionIndex){
            CompletableFuture<Boolean> futureB=setData("/" + id + "/password", newPassword)
                .exceptionally(ex -> {
                    System.out.println("Failed to write data: " + ex.getMessage());
                    return false;
                });
            boolean isSuccessful=futureB.join();
            if (!isSuccessful) 
                return 0;
            else
                return 1;
        }
        // failed to change password(=wrong question)
        return 3;
    }

    

    protected <T> CompletableFuture<T> getData(String path, Class<T> type){ //complete
        CompletableFuture<T> future = new CompletableFuture<>();
        ref = FirebaseDatabase.getInstance().getReference("users");
        if(userId != null)
            ref = ref.child(userId);
        String[] keyParts = path.split("/");
        for(String key : keyParts){
            ref = ref.child(key);
        }
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        T value = dataSnapshot.getValue(type); 
                        future.complete(value);
                    } catch (Exception e) {
                        future.completeExceptionally(new Exception("Failed to cast data to " + type.getName(), e));
                    }
                } else {
                    System.out.println("Data not found at: " + path);
                    future.complete(null); // return null if data not found
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new Exception(databaseError.getMessage()));
            }
        });
            
        return future;
    }

    protected CompletableFuture<Boolean> setData(String path, String data){ //complete
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
            getData(path,String[].class).thenAccept(storedSche -> {
                try {
                    List<String> tempList = new ArrayList<>(Arrays.asList(storedSche));
                    tempList.add(data);
                    storedSche = tempList.toArray(new String[0]);
                    
                    ApiFuture<Void> apiFuture = ref.setValueAsync(storedSche);
                    toCompletableFuture(apiFuture).thenAccept(aVoid -> {
                        System.out.println("Data successfully written at " + path);
                        future.complete(true);
                    }).exceptionally(e -> {
                        System.err.println("Failed to write data at " + path + ": " + e.getMessage());
                        future.complete(false);
                        return null;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    future.complete(false);
                }
            }).exceptionally(ex -> {
                System.out.println("Failed to read data: " + ex.getMessage());
                future.complete(false);
                return null;
            });
        } else {
            ApiFuture<Void> apiFuture = ref.setValueAsync(data);
            toCompletableFuture(apiFuture).thenAccept(aVoid -> {
                System.out.println("Data successfully written at " + path);
                future.complete(true);
            }).exceptionally(e -> {
                System.err.println("Failed to write data at " + path + ": " + e.getMessage());
                future.complete(false);
                return null;
            });
        }
        return future;
    }

    protected CompletableFuture<Boolean> delData(String path,int index){    //complete
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        boolean isExistTestData = true;
        ref = FirebaseDatabase.getInstance().getReference("users");
        String[] keyParts = path.split("/");
        for(String key : keyParts){
            if(!key.equals("test")) // always need for testData
                isExistTestData = false;
            ref = ref.child(key);
        }
        if(!isExistTestData){   // if testData is not exist, create testData. if failed, return false now.
            CompletableFuture<Boolean> t= setData("test/", "This is test data.")
                .exceptionally(ex -> {
                    System.out.println("Failed to create test data: " + ex.getMessage());
                    return false;
                });
            boolean creatTestData=t.join();
            if(!creatTestData){
                future.complete(false);
                return future;
            }
        }
        
        ApiFuture<Void> apiFuture = ref.removeValueAsync();
        toCompletableFuture(apiFuture).thenAccept(aVoid -> {
            System.out.println("Data successfully deleted at " + path);
            future.complete(true);
        }).exceptionally(e -> {
            System.err.println("Failed to delete data at " + path + ": " + e.getMessage());
            future.complete(false);
            return null;
        });

        return future;
    }

    private class AutoLoginData {   //used in autoLogin()
        private final String id;
        private final String str;
        private final String key;

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
    public void test(){
        System.out.println("test");
    }

    public static void main(String[] args) {
        System.out.println("Register Test Start");
        Register registers = new Register();
        Register.test();
        //registers.register("testcase1", "thisispassword123", 2, "testAns");
        //register.login("test", "test", false);
        //register.autoLogin();
        //register.findPassword("test", 0, "test", "test");
        //register.getKeyArray(false);
        //register.getData("/testid/password");
        //register.setData("/test/password", "test");
        //register.delData("/test", 0);
        //System.out.println("Register Test End");
    }
}
