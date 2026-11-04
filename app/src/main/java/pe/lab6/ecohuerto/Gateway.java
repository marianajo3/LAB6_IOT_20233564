package pe.lab6.ecohuerto;
import android.app.Activity;
import android.content.Intent;
import java.util.List;
public interface Gateway  {
    interface Done  {
        void finish(String error);
    }
    interface Data  {
        void update(List<Reading> items,String error);
    }
    boolean configured();
    boolean signedIn();
    String email();
    String mode();
    void login(String email,String password,Done cb);
    void register(String email,String password,Done cb);
    void reset(String email,Done cb);
    void provider(String name,Activity activity,Done cb);
    void guest(Done cb);
    void logout(Done cb);
    void listen(Data cb);
    void stop();
    void save(Reading r,Done cb);
    void delete(String id,Done cb);
    void onActivityResult(int request,int result,Intent data);
}
