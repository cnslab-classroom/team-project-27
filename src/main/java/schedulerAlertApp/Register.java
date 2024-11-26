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

    private static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {   //To be deleted
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
                Object value = snapshot.getValue(String.class);
                if(type.equals(List.class))
                    value = stringToList((String) value);
                System.out.print(value.toString());
                returnFuture.complete((T) value);
            }else{
                System.out.println("No data found at the specified path.");
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

    public static List stringToList(String listAsString) { //debugging complete
        // Remove square brackets
        String trimmedString = listAsString.substring(1, listAsString.length() - 1);
    
        // Handle empty case
        if (trimmedString.isEmpty()) {
            return new ArrayList<>();
        }
    
        // Split by ", " and return as List
        return new ArrayList<>(Arrays.asList(trimmedString.split(", ")));
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
                    System.out.println("Failed to create test data: " + ex.getMessage());
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
                System.out.println("Password updated successfully.");
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
                System.out.println("Data deleted successfully!");
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

    public void setUserId(String id){
        this.userId = id;
        System.out.println("userId set to: " + id);
    }

    public static void main(String[] args) {
        System.out.println("Register Test Start");
        Register registers = new Register();
        CompletableFuture<Boolean> future = registers.register("yourmrmrm", "hahahah", 21, "wtff");
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
        /*
        registers.setUserId("abcd");
        CompletableFuture<String[]> future = registers.getKeyArray(true);
        future.thenAccept(keys -> {
            for (String key : keys) {
                System.out.println("Key: " + key);
            }
        }).exceptionally(e -> {
            System.err.println("Error getting keys: " + e.getMessage());
            return null;
        });
        try {
            future.get(); // wait for the asynchronous task to complete
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        /*
        CompletableFuture<Boolean> setFuture = registers.setData("abcd/schedules/20241121", "dw1");
        setFuture.thenApply(result -> {
            System.out.println("Password updated successfully.");
            return true;
        }).exceptionally(e -> {
            System.err.println("Error setting password: " + e.getMessage());
            return false;
        });
        try {
            setFuture.get(); // 비동기 작업이 끝날 때까지 대기
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        /*
        CompletableFuture<List> getFuture = registers.getData("test123/a1/a2/a3", List.class);
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
        for(String s : testArray)
            System.out.println("Data retrieved: " + s);
        */
        /* 
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
        */
        System.out.println("Register Test End");
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
