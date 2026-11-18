package pe.lab6.alerts;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
@RestController
@RequestMapping("/api/alerts")
public class AlertController {
 public record Measurement(String deviceId,double humidity,double temperature,Instant recordedAt,int servedBy) {}
 public record Alert(String deviceId,String message,boolean needsAttention,int measurementsInstance) {}
 private final RestTemplate client;
 public AlertController(RestTemplate client){this.client=client;}
 @GetMapping("/{deviceId}")
 public Alert check(@PathVariable String deviceId){
  try {
   // Logical service name: Spring Cloud LoadBalancer resolves a registered instance.
   Measurement m=client.getForObject("http://measurements-service/api/measurements/latest?deviceId={id}",Measurement.class,deviceId);
   if(m==null)throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Respuesta vacía de mediciones");
   return new Alert(m.deviceId(),AlertPolicy.evaluate(m.humidity(),m.temperature()),m.humidity()<30||m.temperature()>35,m.servedBy());
  }catch(HttpClientErrorException.NotFound e){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Primero registra una medición");}
   catch(RestClientException|IllegalStateException e){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Mediciones no disponible. Intenta nuevamente.");}
 }
}
