package schedulerAlertApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Register {
    private String userId;
    private DatabaseReference ref;

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
            userData.put("password", password);
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
            List<String> userIdsList = getKeyArray().;
            if(userIds==null)
                throw new Exception("getKeyArray() is failed");
            List<String> userIdsList = Arrays.asList(getKeyArray());
            if(!userIdsList.contains(id))
                return 2;
            //Check password
            ref.child(id).child(password);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if(!password.equals(dataSnapshot.getValue(String.class)))
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
                //Create autoLoginStr 
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

    protected String[] getKeyArray(){
        try {
            List<String> userIds = new ArrayList<>();
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // 모든 키 출력
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        userIds.add(child.getKey());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Error: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return userIds.toArray(new String[0]);
    }

    protected Array<Stiring> getData(String path){
        return null;
    }

    protected boolean setData(String path, String data){
        return true;
    }

    protected boolean delData(String path,int index){
        return true;
    }
}
