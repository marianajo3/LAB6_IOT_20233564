package pe.lab6.ecohuerto;
import android.app.Activity;
import android.content.*;
import org.json.*;
import java.util.*;
// DEMO: acceso de práctica, datos locales. No simula un proveedor OAuth ni envía correos.
public class Backend implements Gateway  {
    private final SharedPreferences prefs;
    private Data listener;
    public Backend(Activity a) {
        prefs=a.getSharedPreferences("demo",0);
    }
    public boolean configured() {
        return true;
    }
    public boolean signedIn() {
        return prefs.getBoolean("session",false);
    }
    public String email() {
        return "Sesión de práctica local";
    }
    public String mode() {
        return "DEMO · datos locales";
    }
    public void guest(Done cb) {
        prefs.edit().putBoolean("session",true).apply();
        cb.finish(null);
    }
    public void login(String e,String p,Done cb) {
        cb.finish("El acceso por correo requiere la versión Firebase. Para practicar, usa Entrar a la demo.");
    }
    public void register(String e,String p,Done cb) {
        cb.finish("El registro real requiere la versión Firebase. La demo no almacena contraseñas.");
    }
    public void reset(String e,Done cb) {
        cb.finish("No se envió ningún correo. Configura Firebase para recuperar contraseñas reales.");
    }
    public void provider(String n,Activity a,Done cb) {
        cb.finish(n+" requiere la versión Firebase y la configuración de su proveedor. Usa Entrar a la demo.");
    }
    public void logout(Done cb) {
        stop();
        prefs.edit().putBoolean("session",false).apply();
        cb.finish(null);
    }
    private List<Reading> all() {
        List<Reading> out=new ArrayList<>();
        try {
            JSONObject j=new JSONObject(prefs.getString("records","{}"));
            Iterator<String> it=j.keys();
            while(it.hasNext()) {
                String id=it.next();
                out.add(Reading.from(id,j.getJSONObject(id)));
            }
        }
        catch(JSONException e) {
            throw new IllegalStateException("Datos demo dañados",e);
        }
        return out;
    }
    public void listen(Data cb) {
        listener=cb;
        cb.update(all(),null);
    }
    public void stop() {
        listener=null;
    }
    private void persist(List<Reading> list,Done cb) {
        JSONObject j=new JSONObject();
        try {
            for(Reading r:list)j.put(r.id,r.json());
        }
        catch(JSONException e) {
            cb.finish("No se pudo serializar el registro");
            return;
        }
        if(!prefs.edit().putString("records",j.toString()).commit()) {
            cb.finish("No se pudo guardar en el dispositivo");
            return;
        }
        if(listener!=null)listener.update(all(),null);
        cb.finish(null);
    }
    public void save(Reading r,Done cb) {
        List<Reading> list=all();
        if(r.id.isEmpty())r.id=UUID.randomUUID().toString();
        list.removeIf(x->x.id.equals(r.id));
        list.add(r);
        persist(list,cb);
    }
    public void delete(String id,Done cb) {
        List<Reading> list=all();
        list.removeIf(x->x.id.equals(id));
        persist(list,cb);
    }
    public void onActivityResult(int a,int b,Intent c) {
    }
}
