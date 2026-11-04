package pe.lab6.ecohuerto;
import org.json.*;
import java.util.*;
public class Reading  {
    public String id="", device="", group="", date="", notes="";
    public double value;
    public Reading()  {
    }
    public Map<String,Object> map() {
        Map<String,Object> m=new HashMap<>();
        m.put("device",device);
        m.put("group",group);
        m.put("date",date);
        m.put("notes",notes);
        m.put("value",value);
        return m;
    }
    public JSONObject json() {
        return new JSONObject(map());
    }
    public static Reading from(String id,JSONObject j) {
        Reading r=new Reading();
        r.id=id;
        r.device=j.optString("device");
        r.group=j.optString("group");
        r.date=j.optString("date");
        r.notes=j.optString("notes");
        r.value=j.optDouble("value",0);
        return r;
    }
}
