package pe.lab6.ecohuerto;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.*;
public class Reading {
 public String id="",course="",activity="Teoría",date="";
 public double hours;
 public String validationError(){
  if(course.trim().isEmpty()||course.length()>100)return "Escribe un curso de 1 a 100 caracteres.";
  try{if(!LocalDate.parse(date).toString().equals(date))return "Fecha inválida.";}catch(Exception e){return "Selecciona una fecha válida.";}
  if(!Double.isFinite(hours)||hours<=0||hours>24)return "Las horas deben ser mayores que 0 y como máximo 24.";
  if(!Arrays.asList(Config.ACTIVITIES).contains(activity))return "Selecciona un tipo de actividad válido.";
  return null;
 }
 public Map<String,Object> map(){
  Map<String,Object> m=new HashMap<>();m.put("course",course.trim());m.put("date",date);m.put("hours",hours);m.put("activity",activity);return m;
 }
 public static Reading from(String id,JSONObject j){
  Reading r=new Reading();r.id=id;r.course=j.optString("course");r.date=j.optString("date");r.activity=j.optString("activity");r.hours=j.optDouble("hours",0);return r;
 }
}
