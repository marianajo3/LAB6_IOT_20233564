package pe.lab6.alerts;
import org.springframework.context.annotation.*;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
@Configuration
public class HttpConfig {
 @Bean @LoadBalanced
 RestTemplate measurementsClient(){
  SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
  factory.setConnectTimeout(2000);factory.setReadTimeout(3000);
  return new RestTemplate(factory);
 }
}
