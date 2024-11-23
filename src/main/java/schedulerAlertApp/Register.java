package schedulerAlertApp;

import com.google.firebase.database.DatabaseReference;  //Firebase library
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener; 
import com.fasterxml.jackson.databind.ObjectMapper; //Jackson library

import org.checkerframework.checker.units.qual.g;
import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.t;
import org.mindrot.jbcrypt.BCrypt;  //JBCrypt library

import java.io.File;
import java.security.SecureRandom;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Register { //complete
    private String userId;
    private DatabaseReference ref;
    private static final int GENSALTNUM = 12;
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public Register(){  //complete
        this.userId = null;
        try {
            FirebaseInit.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Check if "autoLoginData.json" exists
        String fileName = "autoLoginData.json";
        File file = new File(fileName);
        // if "autoLoginData.json" does not exist, create it with default data
        if (!file.exists() && file.isFile()) {
            ObjectMapper objectMapper = new ObjectMapper();
            AutoLoginData autoLoginData = new AutoLoginData("test", "test", "test");
            objectMapper.writeValue(new File("autoLoginData.json"), autoLoginData);
        }
    }

    protected boolean register(String id, String password, int questionIndex, String questionAns){  //complete
        // Refer to the "users" path
        ref = FirebaseDatabase.getInstance().getReference("users");
        // Organize user register data
        CompletableFuture<Boolean> asyncTask = new CompletableFuture<>();
        boolean isSuccessful = false;
        Map<String, Object> userData = new HashMap<>(); 
        userData.put("password", BCrypt.hashpw(password, BCrypt.gensalt(GENSALTNUM)));
        userData.put("questionIndex", questionIndex);
        userData.put("questionAns", questionAns);
        userData.put("autoLoginStr", "NULL");
        userData.put("autoLoginKey", "NULL");
        userData.put("schedules", new HashMap<>());
        // Set userData
        databaseReference.child(id).setValue(userData)
            .addOnSuccessListener(aVoid -> {
                System.out.println("uesrData successfully created.");
                isSuccessful = true;
                asyncTask.complete(true);
            })
            .addOnFailureListener(e -> {
                System.err.println("Failed to create uesrData: " + e.getMessage());
                asyncTask.complete(false);
            });
        // check setValue(userData) is successed
        boolean result = asyncTask.join(); // Wait for the task to complete
        if (result) {
            System.out.println("Operation completed successfully!");
        } else {
            System.err.println("Operation failed.");
        }
        if(isSuccessful)
            return true;
        return false;
    }

    protected int login(String id, String password, boolean autoLogin){ //complete
        //Check id
        ref = FirebaseDatabase.getInstance().getReference("users");
        List<String> userIdList = null;
        getKeyArray(false).thenAccept(userIdsList -> {
            userIdList = Arrays.asList(userIdsList);
        }).exceptionally(ex -> {
            System.err.println("Failed to retrieve userIdsList: " + ex.getMessage());
            return 0;
        });
        if(!userIdList.contains(id))
            return 2;

        //Check password 
        String storedPassword = null;
        getData("/" + id + "/password").thenAccept(getPassword -> {
            storedPassword = getPassword;
        }).exceptionally(ex -> {
            System.out.println("Failed to read data: " + ex.getMessage());
            return 0;
        });
        if(!storedPassword.equals(password))
            return 3;

        //Check autoLogin
        boolean isSuccessfulAutoLogin = true;
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
            setData("/" + id + "/autoLoginStr", hashStr).thenAccept(null)
                .exceptionally(ex -> {
                    System.out.println("Failed to write data: " + ex.getMessage());
                    isSuccessfulAutoLogin = false;
                });
            setData("/" + id + "/autoLoginKey", randomKey).thenAccept(null)
                .exceptionally(ex -> {
                    System.out.println("Failed to write data: " + ex.getMessage());
                    isSuccessfulAutoLogin = false;
                });
            //Successed autoLogin
            if(isSuccessfulAutoLogin){
                ObjectMapper objectMapper = new ObjectMapper();
                AutoLoginData autoLoginData = new AutoLoginData(id, randomStr, hashKey);
                objectMapper.writeValue(new File("autoLoginData.json"), autoLoginData);
            }
            else{   //Failed autoLogin
                return 4;
            }
        }
        return 1;
    }

    protected boolean autoLogin(){  //complete
        ObjectMapper objectMapper = new ObjectMapper();
        AutoLoginData autoLoginData = objectMapper.readValue(new File("autoLoginData.json"), AutoLoginData.class);

        //Check autoLogin
        //1. Check id
        List<String> userIdList = null;
        ref = FirebaseDatabase.getInstance().getReference("users");
        getKeyArray(false).thenAccept(userIdsList -> {
            userIdList = Arrays.asList(userIdsList);
        }).exceptionally(ex -> {
            System.err.println("Failed to retrieve userIdsList: " + ex.getMessage());
            return 0;
        });
        if(!userIdList.contains(autoLoginData.getId()))
            return false;
        String id = autoLoginData.getId();
        //2. Check autoLoginStr and autoLoginKey
        String storedStrFB = null;
        String storedKeyFB = null;
        getData("/" + id + "/autoLoginStr").thenAccept(getStr -> {
            storedStrFB = getStr;
        }).exceptionally(ex -> {
            System.out.println("Failed to read data: " + ex.getMessage());
            return false;
        });
        getData("/" + id + "/autoLoginKey").thenAccept(getKey -> {
            storedKeyFB = getKey;
        }).exceptionally(ex -> {
            System.out.println("Failed to read data: " + ex.getMessage());
            return false;
        });
        if(BCrypt.checkpw(autoLoginData.getStr(), storedStrFB) && BCrypt.checkpw(storedKeyFB,autoLoginData.getKey())){   //autoLogin successed
            userId = id;
            return true;
        }
        return false;
    }

    protected int findPassword(String id, int questionIndex, String questionAns, String newPassword){   //complete
        // check id
        List<String> userIdList = null;
        getKeyArray(false).thenAccept(getIdsList -> {
            userIdList = Arrays.asList(getIdsList);
        }).exceptionally(ex -> {
            System.err.println("Failed to retrieve getIdsList: " + ex.getMessage());
            return 0;
        });
        if(!userIdList.contains(id))
            return 2;

        // check question
        int storedQuestionIndex = null;
        String storedQuestionAns = null;
        getData("/" + id + "/questionIndex").thenAccept(getIndex -> {
            try {
                storedQuestionIndex = Integer.parseInt(getIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).exceptionally(ex -> {
            System.out.println("Failed to read data: " + ex.getMessage());
            return 0;
        });
        getData("/" + id + "/questionAns").thenAccept(getAns -> {
            storedQuestionAns = getAns;
        }).exceptionally(ex -> {
            System.out.println("Failed to read data: " + ex.getMessage());
            return 0;
        });

        // change password
        if(storedQuestionAns.equals(questionAns)&&storedQuestionIndex==questionIndex){
            setData("/" + id + "/password", newPassword).thenAccept(null)
                .exceptionally(ex -> {
                    System.out.println("Failed to write data: " + ex.getMessage());
                    return 0;
                });
            return 1;
        }
        // failed to change password(=wrong question)
        return 3;
    }

    protected CompletableFuture<String[]> getKeyArray(boolean isSchedules){ //complete
        CompletableFuture<String[]> future = new CompletableFuture<>();
        ref = FirebaseDatabase.getInstance().getReference("users");
        if(isSchedules == true)
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

    protected CompletableFuture<String[]> getData(String path){ //complete
        CompletableFuture<String[]> future = new CompletableFuture<>();
        ref = FirebaseDatabase.getInstance().getReference("users");
        if(userId != null)
            ref = FirebaseDatabase.getInstance().getReference(userId);
        string[] keyParts = path.split("/");
        for(String key : keyParts){
            ref = ref.child(key);
        }
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    future.complete(dataSnapshot.getValue(String[].class));
                } else {
                    System.out.println("scheDatas are not exist.");
                    future.complete(new String[0]);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new Exception(databaseError.getMessage()));
            }
        });
        if(dataArray == null)
            
        return future;
    }

    protected CompletableFuture<Boolean> setData(String path, String data){ //complete
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        boolean addSchedule = false;
        ref = FirebaseDatabase.getInstance().getReference("users");
        if(userId != null)
            ref = FirebaseDatabase.getInstance().getReference(userId);
        string[] keyParts = path.split("/");
        for(String key : keyParts){
            if(key.equals("schedules"))
                addSchedule = true;
            ref = ref.child(key);
        }

        if(addSchedule){
            getData(path).thenAccept(storedSche -> {

                try {
                    List<String> tempList = new ArrayList<>(Arrays.asList(storedSche));
                    tempList.add(data);
                    storedSche = tempList.toArray(new String[0]);

                    ref.setValue(storedSche, (error, ref) -> {
                        if (error != null) {
                            System.out.println("setData failed: " + error.getMessage());
                            future.complete(false);
                        } else {
                            System.out.println("setData succeeded: " + ref.getPath());
                            future.complete(true);
                        }
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
            ref.setValue(data, (error, ref) -> {
                if (error != null) {
                    System.out.println("setData is failed: " + error.getMessage());
                    future.complete(false);
                } else {
                    System.out.println("setData is successed: " + ref.getPath());
                    future.complete(true);
                }
            });
        }
        return future;
    }

    protected CompletableFuture<boolean> delData(String path,int index){    //complete
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        boolean isExistTestData = true;
        ref = FirebaseDatabase.getInstance().getReference("users");
        string[] keyParts = path.split("/");
        for(String key : keyParts){
            if(!key.equlas("test")) // always need for testData
                isExistTestData = false;
            ref = ref.child(key);
        }
        if(!isExistTestData){   // if testData is not exist, create testData. if failed, return false now.
            boolean creatTestData = setData("test/", "This is test data.").thenAccept(null)
                .exceptionally(ex -> {
                    System.out.println("Failed to create test data: " + ex.getMessage());
                    return future.complete(false);
                });
            if(!creatTestData)
                return future.complete(false);
        }
        
        ref.removeValue()
            .addOnSuccessListener(aVoid -> {
                System.out.println("data successfully removed.");
                future.complete(true);
            })
            .addOnFailureListener(e -> {
                System.err.println("Failed to remove data: " + e.getMessage());
                return future.complete(false);
            });
        
        return future.complete(false);
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
}
