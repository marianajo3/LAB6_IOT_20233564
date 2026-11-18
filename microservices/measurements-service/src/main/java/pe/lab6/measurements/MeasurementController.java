package pe.lab6.measurements;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {
 public record Input(@NotBlank @Size(max=60) String deviceId,
   @NotNull @DecimalMin("0") @DecimalMax("100") Double humidity,
   @NotNull @DecimalMin("-20") @DecimalMax("80") Double temperature) {}
 public record Measurement(String deviceId,double humidity,double temperature,Instant recordedAt,int servedBy) {}
 private final ConcurrentHashMap<String,Measurement> latest=new ConcurrentHashMap<>();
 private final int port;
 public MeasurementController(@Value("${server.port}") int port){this.port=port;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public Measurement create(@Valid @RequestBody Input input){
  Measurement m=new Measurement(input.deviceId(),input.humidity(),input.temperature(),Instant.now(),port);
  latest.put(input.deviceId(),m);return m;
 }
 @GetMapping("/latest")
 public Measurement latest(@RequestParam String deviceId){
  Measurement found=latest.get(deviceId);
  if(found==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No hay mediciones para ese dispositivo");
  return found;
 }
}
