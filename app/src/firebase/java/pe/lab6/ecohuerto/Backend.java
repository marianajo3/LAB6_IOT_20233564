package pe.lab6.ecohuerto;

import android.app.Activity;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import java.util.*;
import org.json.JSONObject;

/** All documents are scoped to the authenticated Firebase UID. */
public class Backend implements Gateway {
    private final Activity activity;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration subscription;
    public Backend(Activity activity) {
        this.activity=activity;
        if(FirebaseApp.initializeApp(activity)!=null) {
            auth=FirebaseAuth.getInstance();
            auth.setLanguageCode("es");
            db=FirebaseFirestore.getInstance();
        }
    }
    public boolean configured(){return auth!=null;}
    public boolean signedIn(){return configured() && auth.getCurrentUser()!=null;}
    public String email(){return signedIn()?Objects.toString(auth.getCurrentUser().getEmail(),"Estudiante"):"";}
    private String message(Exception e){
        if(e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException)e).getCode()==FirebaseFirestoreException.Code.PERMISSION_DENIED)
            return "No tienes permiso para estos datos. Revisa las reglas de Firestore y tu sesión.";
        return "No se completó la operación. Revisa tu conexión y vuelve a intentarlo.";
    }
    public void logout(Done cb){
        stop();
        AuthUI.getInstance().signOut(activity).addOnCompleteListener(activity,t->cb.finish(t.isSuccessful()?null:message(t.getException())));
    }
    private CollectionReference collection(){
        if(!signedIn())throw new IllegalStateException("Sesión requerida");
        return db.collection("users").document(auth.getUid()).collection("sessions");
    }
    public void listen(Data cb){
        stop();
        if(!signedIn()){cb.update(new ArrayList<>(),"Debes iniciar sesión."); return;}
        subscription=collection().addSnapshotListener((snapshot,e)->{
            if(e!=null){cb.update(new ArrayList<>(),message(e));return;}
            List<Reading> rows=new ArrayList<>();
            if(snapshot!=null)for(DocumentSnapshot d:snapshot.getDocuments())
                rows.add(Reading.from(d.getId(),new JSONObject(d.getData())));
            rows.sort(Comparator.comparing((Reading r)->r.date).reversed().thenComparing(r->r.course));
            cb.update(rows,null);
        });
    }
    public void stop(){if(subscription!=null){subscription.remove();subscription=null;}}
    public void save(Reading r,Done cb){
        if(!signedIn()){cb.finish("Tu sesión terminó. Vuelve a iniciar sesión.");return;}
        String error=r.validationError();
        if(error!=null){cb.finish(error);return;}
        DocumentReference ref=r.id.isEmpty()?collection().document():collection().document(r.id);
        // Updating cannot recreate an item removed on another device.
        com.google.android.gms.tasks.Task<Void> task=r.id.isEmpty()?ref.set(r.map()):ref.update(r.map());
        task.addOnCompleteListener(activity,t->{if(t.isSuccessful())r.id=ref.getId();cb.finish(t.isSuccessful()?null:message(t.getException()));});
    }
    public void delete(String id,Done cb){
        if(!signedIn()){cb.finish("Tu sesión terminó.");return;}
        collection().document(id).delete().addOnCompleteListener(activity,t->cb.finish(t.isSuccessful()?null:message(t.getException())));
    }
}
