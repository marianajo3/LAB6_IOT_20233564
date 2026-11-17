package pe.lab6.ecohuerto;
import java.time.YearMonth;
import java.util.*;
/** Pure aggregation shared by drawing and tests: always sums hours, never averages. */
public final class StudyStats {
 public static Map<String,Double> months(List<Reading> rows,String course){
  Map<String,Double> result=new TreeMap<>();
  for(Reading r:rows)if(course==null||course.equals(r.course)){
   String month=YearMonth.from(java.time.LocalDate.parse(r.date)).toString();
   result.put(month,result.getOrDefault(month,0d)+r.hours);
  }
  return result;
 }
 public static Map<String,Double> activities(List<Reading> rows,String course){
  Map<String,Double> result=new LinkedHashMap<>();
  for(String type:Config.ACTIVITIES)result.put(type,0d);
  for(Reading r:rows)if(course==null||course.equals(r.course))result.put(r.activity,result.getOrDefault(r.activity,0d)+r.hours);
  return result;
 }
 private StudyStats(){}
}
