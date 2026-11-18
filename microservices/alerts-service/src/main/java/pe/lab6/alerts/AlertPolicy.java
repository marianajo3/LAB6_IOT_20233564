package pe.lab6.alerts;
public final class AlertPolicy {
 public static String evaluate(double humidity,double temperature){
  if(humidity<30 && temperature>35)return "Humedad baja y temperatura alta";
  if(humidity<30)return "Humedad baja: revisar necesidad de riego";
  if(temperature>35)return "Temperatura alta: revisar condiciones";
  return "Condiciones dentro de los umbrales de la demo";
 }
 private AlertPolicy(){}
}
