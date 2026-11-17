package pe.lab6.ecohuerto;
import java.util.List;
public interface Gateway {
 interface Done {void finish(String error);}
 interface Data {void update(List<Reading> items,String error);}
 boolean configured();
 boolean signedIn();
 String email();
 void logout(Done cb);
 void listen(Data cb);
 void stop();
 void save(Reading session,Done cb);
 void delete(String id,Done cb);
}
