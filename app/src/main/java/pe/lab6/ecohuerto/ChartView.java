package pe.lab6.ecohuerto;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;
// Gráficos nativos: no hay datos precargados ni dependencia de una librería externa.
public class ChartView extends View  {
    private final java.util.List<Reading> rows;
    private final boolean bars;
    private final Paint p=new Paint(3);
    private final int accent;
    public ChartView(Context c,java.util.List<Reading> r,boolean b) {
        super(c);
        rows=new ArrayList<>(r);
        bars=b;
        accent=Color.parseColor(Config.COLOR);
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        setContentDescription(b?"Gráfico de barras: promedio por "+Config.GROUP:"Gráfico de líneas: promedio por fecha");
    }
    private void text(Canvas c,String s,float x,float y,int color,float size) {
        p.setColor(color);
        p.setTextSize(size*getResources().getDisplayMetrics().scaledDensity);
        p.setStyle(Paint.Style.FILL);
        c.drawText(s,x,y,p);
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float density=getResources().getDisplayMetrics().density;
        float l=58*density,t=28*density,r=getWidth()-20*density,b=getHeight()-52*density;
        Map<String,double[]> sums=new TreeMap<>();
        for(Reading row:rows) {
            String key=bars?row.group:row.date;
            double[] pair=sums.computeIfAbsent(key,k->new double[2]);
            pair[0]+=row.value;
            pair[1]++;
        }
        if(sums.isEmpty()) {
            text(c,"Sin datos para este filtro",24*density,getHeight()/2f,Color.GRAY,15);
            return;
        }
        java.util.List<String> keys=new ArrayList<>(sums.keySet());
        double max=0,min=0;
        for(double[] pair:sums.values()) {
            double value=pair[0]/pair[1];
            max=Math.max(max,value);
            min=Math.min(min,value);
        }
        if(max==min)max=min+1;
        double range=max-min;
        max+=range*.12;
        if(min<0)min-=range*.12;
        for(int i=0; i<=4; i++) {
            float y=b-(b-t)*i/4;
            p.setColor(0xFFE0E5E9);
            p.setStrokeWidth(density);
            c.drawLine(l,y,r,y,p);
            text(c,String.format(Locale.US,"%.1f",min+(max-min)*i/4),3*density,y+4*density,0xFF65717E,10);
        }
        Path path=new Path();
        int n=keys.size();
        for(int i=0; i<n; i++) {
            double[] pair=sums.get(keys.get(i));
            double val=pair[0]/pair[1];
            float x=bars?l+(r-l)*(i+.5f)/n:(n==1?(l+r)/2:l+(r-l)*i/(n-1));
            float y=(float)(b-(val-min)/(max-min)*(b-t));
            p.setColor(accent);
            p.setStyle(Paint.Style.FILL);
            if(bars) {
                float zero=(float)(b-(0-min)/(max-min)*(b-t));
                float half=Math.min((r-l)/n*.27f,35*density);
                c.drawRoundRect(x-half,Math.min(zero,y),x+half,Math.max(zero,y)+density,5*density,5*density,p);
                text(c,String.format(Locale.US,"%.1f",val),x-12*density,y-8*density,accent,11);
            }
            else {
                if(i==0)path.moveTo(x,y);
                else path.lineTo(x,y);
                c.drawCircle(x,y,4*density,p);
            }
            if(bars||i==0||i==n-1||(n<=5)) {
                String key=keys.get(i);
                if(!bars&&key.length()>=10)key=key.substring(5);
                if(key.length()>12)key=key.substring(0,11)+"…";
                p.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);
                float width=p.measureText(key);
                text(c,key,Math.max(l,Math.min(x-width/2,r-width)),b+22*density,0xFF65717E,10);
            }
        }
        if(!bars) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3*density);
            p.setColor(accent);
            c.drawPath(path,p);
            p.setStyle(Paint.Style.FILL);
        }
    }
}
