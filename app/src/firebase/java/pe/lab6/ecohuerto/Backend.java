package pe.lab6.ecohuerto;
import android.app.Activity;
import android.content.Intent;
import androidx.credentials.*;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.core.content.ContextCompat;
import com.google.android.libraries.identity.googleid.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.facebook.*;
import com.facebook.login.*;
import java.util.*;
import org.json.JSONObject;
public class Backend implements Gateway  {
    private final Activity activity;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration subscription;
    private CallbackManager facebook;
    public Backend(Activity a) {
        activity=a;
        if(FirebaseApp.initializeApp(a)!=null) {
            auth=FirebaseAuth.getInstance();
            auth.setLanguageCode("es");
            db=FirebaseFirestore.getInstance();
        }
    }
    public boolean configured() {
        return auth!=null;
    }
    public boolean signedIn() {
        return configured()&&auth.getCurrentUser()!=null;
    }
    public String email() {
        if(!signedIn())return "";
        FirebaseUser u=auth.getCurrentUser();
        return u.getEmail()!=null?u.getEmail():(u.getDisplayName()!=null?u.getDisplayName():"Usuario autenticado");
    }
    public String mode() {
        return "FIREBASE · Cloud Firestore";
    }
    private boolean ready(Done cb) {
        if(configured())return true;
        cb.finish("Falta app/google-services.json. Sigue CONFIGURAR_FIREBASE.md y vuelve a compilar.");
        return false;
    }
    private String message(Exception e) {
        if(e instanceof FirebaseAuthInvalidCredentialsException)return "Credenciales inválidas. Comprueba el correo y la contraseña.";
        if(e instanceof FirebaseAuthUserCollisionException)return "La cuenta ya existe. Inicia sesión con su proveedor original.";
        if(e instanceof FirebaseAuthWeakPasswordException)return "La contraseña no cumple la política de Firebase.";
        return "No se completó la operación. Revisa conexión, configuración y permisos. Detalle: "+(e==null?"desconocido":e.getLocalizedMessage());
    }
    public void login(String e,String p,Done cb) {
        if(ready(cb))auth.signInWithEmailAndPassword(e,p).addOnCompleteListener(activity,t->cb.finish(t.isSuccessful()?null:message(t.getException())));
    }
    public void register(String e,String p,Done cb) {
        if(ready(cb))auth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(activity,t->cb.finish(t.isSuccessful()?null:message(t.getException())));
    }
    public void reset(String e,Done cb) {
        if(ready(cb))auth.sendPasswordResetEmail(e).addOnCompleteListener(activity,t->cb.finish(t.isSuccessful()?null:message(t.getException())));
    }
    public void guest(Done cb) {
        cb.finish("La versión Firebase requiere una cuenta. La demo se compila sin -Pfirebase=true.");
    }
    public void logout(Done cb) {
        stop();
        if(auth!=null)auth.signOut();
        if(FacebookSdk.isInitialized())LoginManager.getInstance().logOut();
        cb.finish(null);
    }
    public void provider(String name,Activity a,Done cb) {
        if(!ready(cb))return;
        if(name.equals("Google")) {
            int id=a.getResources().getIdentifier("default_web_client_id","string",a.getPackageName());
            if(id==0) {
                cb.finish("Activa Google en Firebase, registra las huellas SHA y descarga de nuevo google-services.json.");
                return;
            }
            GetGoogleIdOption option=new GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setServerClientId(a.getString(id)).setAutoSelectEnabled(false).build();
            GetCredentialRequest request=new GetCredentialRequest.Builder().addCredentialOption(option).build();
            CredentialManager.create(a).getCredentialAsync(a,request,null,ContextCompat.getMainExecutor(a),new CredentialManagerCallback<GetCredentialResponse,GetCredentialException>() {
                public void onResult(GetCredentialResponse result) {
                    try {
                        Credential c=result.getCredential(); if(!(c instanceof CustomCredential)||!c.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            cb.finish("Google devolvió un tipo de credencial no compatible."); return;
                        }
                        GoogleIdTokenCredential token=GoogleIdTokenCredential.createFrom(c.getData()); auth.signInWithCredential(GoogleAuthProvider.getCredential(token.getIdToken(),null)).addOnCompleteListener(a,t->cb.finish(t.isSuccessful()?null:message(t.getException())));
                    }
                    catch(Exception e) {
                        cb.finish(message(e));
                    }
                }
                public void onError(GetCredentialException e) {
                    cb.finish("Acceso con Google cancelado o no disponible: "+e.getLocalizedMessage());
                }
            }
            );
        }
        else {
            if(a.getString(R.string.facebook_app_id).equals("0")||a.getString(R.string.facebook_client_token).equals("CONFIGURAR")) {
                cb.finish("Configura facebook_app_id y facebook_client_token en res/values/facebook.xml.");
                return;
            }
            if(!FacebookSdk.isInitialized())FacebookSdk.sdkInitialize(a.getApplicationContext());
            facebook=CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(facebook,new FacebookCallback<LoginResult>() {
                public void onSuccess(LoginResult r) {
                    auth.signInWithCredential(FacebookAuthProvider.getCredential(r.getAccessToken().getToken())).addOnCompleteListener(a,t->cb.finish(t.isSuccessful()?null:message(t.getException())));
                }
                public void onCancel() {
                    cb.finish("Acceso con Facebook cancelado.");
                }
                public void onError(FacebookException e) {
                    cb.finish(message(e));
                }
            }
            );
            LoginManager.getInstance().logInWithReadPermissions(a,Arrays.asList("public_profile","email"));
        }
    }
    private CollectionReference collection() {
        if(!signedIn())throw new IllegalStateException("Sesión requerida");
        return db.collection("users").document(auth.getUid()).collection("readings");
    }
    public void listen(Data cb) {
        stop();
        if(!signedIn()) {
            cb.update(new ArrayList<>(),"Debes iniciar sesión");
            return;
        }
        subscription=collection().addSnapshotListener((s,e)-> {
            if(e!=null) {
                cb.update(new ArrayList<>(),message(e)); return;
            }
            List<Reading> out=new ArrayList<>(); if(s!=null)for(DocumentSnapshot d:s.getDocuments())out.add(Reading.from(d.getId(),new JSONObject(d.getData()))); cb.update(out,null);
        }
        );
    }
    public void stop() {
        if(subscription!=null) {
            subscription.remove();
            subscription=null;
        }
    }
    public void save(Reading r,Done cb) {
        if(!signedIn()) {
            cb.finish("Sesión cerrada");
            return;
        }
        DocumentReference ref=r.id.isEmpty()?collection().document():collection().document(r.id);
        ref.set(r.map()).addOnCompleteListener(activity,t-> {
            if(t.isSuccessful())r.id=ref.getId(); cb.finish(t.isSuccessful()?null:message(t.getException()));
        }
        );
    }
    public void delete(String id,Done cb) {
        if(!signedIn()) {
            cb.finish("Sesión cerrada");
            return;
        }
        collection().document(id).delete().addOnCompleteListener(activity,t->cb.finish(t.isSuccessful()?null:message(t.getException())));
    }
    public void onActivityResult(int request,int result,Intent data) {
        if(facebook!=null)facebook.onActivityResult(request,result,data);
    }
}
