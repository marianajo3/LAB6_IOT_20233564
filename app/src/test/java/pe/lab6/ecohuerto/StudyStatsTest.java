package pe.lab6.ecohuerto;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class StudyStatsTest {
 private Reading row(String course,String date,double hours,String activity){Reading r=new Reading();r.course=course;r.date=date;r.hours=hours;r.activity=activity;return r;}
 @Test public void sumsHoursAndSeparatesYears(){
  List<Reading> rows=Arrays.asList(row("IoT","2025-11-01",2,"Teoría"),row("IoT","2026-11-01",3,"Práctica"),row("IoT","2026-11-02",1.5,"Laboratorio"));
  assertEquals(2d,StudyStats.months(rows,null).get("2025-11"),0.001);
  assertEquals(4.5,StudyStats.months(rows,null).get("2026-11"),0.001);
 }
 @Test public void sameCourseFilterAppliesToBothCharts(){
  List<Reading> rows=Arrays.asList(row("IoT","2026-01-01",2,"Teoría"),row("Redes","2026-01-01",8,"Práctica"),row("IoT","2026-01-02",1.5,"Teoría"));
  assertEquals(3.5,StudyStats.months(rows,"IoT").get("2026-01"),0.001);
  assertEquals(3.5,StudyStats.activities(rows,"IoT").get("Teoría"),0.001);
  assertEquals(0d,StudyStats.activities(rows,"IoT").get("Práctica"),0.001);
 }
 @Test public void emptyDataDoesNotInventHours(){assertTrue(StudyStats.months(Collections.emptyList(),null).isEmpty());assertEquals(0d,StudyStats.activities(Collections.emptyList(),null).get("Laboratorio"),0);}
 @Test public void validationRejectsInvalidDatesHoursAndTypes(){
  Reading r=row("IoT","2026-02-28",1.25,"Laboratorio");assertNull(r.validationError());
  r.date="2026-02-30";assertNotNull(r.validationError());r.date="2026-02-28";
  for(double hours:new double[]{0,-1,24.01,Double.NaN,Double.POSITIVE_INFINITY}){r.hours=hours;assertNotNull(r.validationError());}
  r.hours=24;assertNull(r.validationError());r.activity="Cultivo A";assertNotNull(r.validationError());
  r.activity="Teoría";r.course="  ";assertNotNull(r.validationError());
 }
}
