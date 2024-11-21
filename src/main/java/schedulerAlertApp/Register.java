package schedulerAlertApp;

import com.google.firebase.database.DatabaseReference;  //Firebase library
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener; 
import com.fasterxml.jackson.databind.ObjectMapper; //Jackson library

import org.checkerframework.checker.units.qual.g;
import org.checkerframework.checker.units.qual.s;
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

public class Register {
    private String userId;
    private DatabaseReference ref;
    private static final int GENSALTNUM = 12;
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public Register(){
        this.userId = null;
        try {
            FirebaseInit.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    protected boolean register(String id, String password, int questionIndex, String questionAns){
        try {
            // Refer to the "users" path
            ref = FirebaseDatabase.getInstance().getReference("users");
            // Organize user register data
            Map<String, Object> userData = new HashMap<>(); 
            userData.put("password", BCrypt.hashpw(password, BCrypt.gensalt(GENSALTNUM)));
            userData.put("questionIndex", questionIndex);
            userData.put("questionAns", questionAns);
            userData.put("autoLoginStr", "NULL");
            userData.put("autoLoginKey", "NULL");
            userData.put("schedules", new HashMap<>());
            // Set userData
            userRef.child(id).setValue(userData, (error, ref) -> {
                if (error != null) {
                    System.out.println("Register is failed: " + error.getMessage());
                } else {
                    System.out.println("Register is successed: " + ref.getPath());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected int login(String id, String password, boolean autoLogin){
        try {
            //Check id
            ref = FirebaseDatabase.getInstance().getReference("users");
            getKeyArray().thenAccept(userIdsList -> {
                List<String> keyList = Arrays.asList(userIdsList);
            }).exceptionally(ex -> {
                System.err.println("Failed to retrieve userIdsList: " + ex.getMessage());
                return null;
            });
            if(userIds==null)
                throw new Exception("getKeyArray() is failed");
            List<String> userIdsList = Arrays.asList(getKeyArray());
            if(!userIdsList.contains(id))
                return 2;
            //Check password        //비동기 처리 하기
            ref.child(id).child(password);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if(!BCrypt.checkpw(password, dataSnapshot.getValue(String.class))) 
                            return 3;
                    } else {
                        System.out.println("Password not found at the specified path.");
                        return 0;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Error: " + databaseError.getMessage());
                    return 0;
                }
            });
            //Check autoLogin
            if(autoLogin){
                SecureRandom random = new SecureRandom();
                String randomStr = IntStream.range(0, 8) 
                        .mapToObj(i -> String.valueOf(CHARSET.charAt(random.nextInt(CHARSET.length()))))
                        .collect(Collectors.joining());
                String randomKey = IntStream.range(0, 8)
                        .mapToObj(i -> String.valueOf(CHARSET.charAt(random.nextInt(CHARSET.length()))))
                        .collect(Collectors.joining());
                String hashStr = BCrypt.hashpw(randomStr, BCrypt.gensalt(GENSALTNUM));
                String hashKey = BCrypt.hashpw(randomKey, BCrypt.gensalt(GENSALTNUM));
                Map<String, Object> autoLoginData = new HashMap<>();
                autoLoginData.put("Id", id);
                autoLoginData.put("Str", randomStr);
                autoLoginData.put("Key", hashKey);
                //more code
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected boolean autoLogin(){
        return true;
    }

    protected int findPassword(String id, int questionIndex, String questionAns, String newPassword){
        return 0;
    }

    protected CompletableFuture<String[]> getKeyArray(){
        CompletableFuture<String[]> future = new CompletableFuture<>();
        
        List<String> userIds = new ArrayList<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 모든 키 출력
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    userIds.add(child.getKey());
                }
                future.complete(userIds.toArray(new String[0]));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error: " + databaseError.getMessage());
                future.completeExceptionally(new Exception(databaseError.getMessage()));
            }
        });
        
        return future;
    }

    protected CompletableFuture<String[]> getData(String path){
        CompletableFuture<String[]> future = new CompletableFuture<>();

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

    protected CompletableFuture<Boolean> setData(String path, String data){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try{
            boolean addSchedule = false;
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
        }
        catch (Exception e) {
            e.printStackTrace();
            future.complete(false);
        }
        return future;
    }

    protected boolean delData(String path,int index){
        return true;
    }
}
