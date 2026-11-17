package pe.lab6.ecohuerto;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;
public class ChartView extends View {
 private final Map<String,Double> values;
 private final boolean bars;
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final int[] colors={0xFF137A63,0xFF4678D4,0xFFEA9D33};
 public ChartView(Context context,Map<String,Double> values,boolean bars){
  super(context);this.values=new LinkedHashMap<>(values);this.bars=bars;
  setContentDescription(bars?"Horas totales por mes; detalle debajo de los gráficos":"Proporción de horas por actividad; leyenda debajo del gráfico");
 }
 private float dp(float n){return n*getResources().getDisplayMetrics().density;}
 private void label(Canvas c,String text,float x,float y){p.setColor(0xFF334155);p.setTextSize(dp(11));c.drawText(text,x,y,p);}
 @Override protected void onDraw(Canvas c){
  super.onDraw(c);double total=0,max=0;for(double v:values.values()){total+=v;max=Math.max(max,v);}
  if(total==0){label(c,"Sin datos para este filtro",dp(16),getHeight()/2f);return;}
  if(bars){
   float left=dp(48),bottom=getHeight()-dp(42),top=dp(28),width=getWidth()-left-dp(16);int n=values.size();
   for(int i=0;i<=4;i++){float y=bottom-(bottom-top)*i/4;p.setColor(0xFFDEE5EA);c.drawLine(left,y,getWidth()-dp(12),y,p);label(c,String.format(Locale.US,"%.1f",max*i/4),dp(2),y);}
   int i=0;for(Map.Entry<String,Double> e:values.entrySet()){
    float x=left+width*(i+.5f)/n,y=bottom-(float)(e.getValue()/max)*(bottom-top),half=Math.min(dp(25),width/n*.3f);
    p.setColor(colors[0]);c.drawRect(x-half,y,x+half,bottom,p);
    label(c,String.format(Locale.US,"%.2f",e.getValue()),x-dp(16),y-dp(7));label(c,e.getKey(),x-dp(23),bottom+dp(22));i++;
   }
  }else{
   float size=Math.min(getWidth()-dp(28),getHeight()-dp(36)),cx=getWidth()/2f,cy=getHeight()/2f;
   RectF oval=new RectF(cx-size/2,cy-size/2,cx+size/2,cy+size/2);float start=-90;int i=0;
   for(Map.Entry<String,Double> e:values.entrySet()){
    float sweep=(float)(360*e.getValue()/total);p.setColor(colors[i++%colors.length]);c.drawArc(oval,start,sweep,true,p);
    if(sweep>20){double angle=Math.toRadians(start+sweep/2);p.setColor(Color.WHITE);p.setTextSize(dp(13));String s=String.format(Locale.US,"%.0f%%",100*e.getValue()/total);c.drawText(s,cx+(float)Math.cos(angle)*size*.32f-dp(12),cy+(float)Math.sin(angle)*size*.32f,p);}
    start+=sweep;
   }
  }
 }
}
