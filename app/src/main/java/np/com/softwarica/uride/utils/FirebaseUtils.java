package np.com.softwarica.uride.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {
    public static DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    public static StorageReference storage = FirebaseStorage.getInstance().getReference();
    public static FirebaseAuth auth = FirebaseAuth.getInstance();
    public static FirebaseUser user = auth.getCurrentUser();
    public static String userId = user.getUid();
}
