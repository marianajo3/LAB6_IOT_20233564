package pe.lab6.ecohuerto;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.firebase.ui.auth.*;
import java.time.LocalDate;
import java.util.*;

public class MainActivity extends AppCompatActivity {
 private Gateway backend;
 private LinearLayout body,content;
 private final List<Reading> all=new ArrayList<>();
 private String screen="login",selectedCourse=null;
 private boolean busy=false;
 private static final int AUTH=100;
 @Override public void onCreate(Bundle state){
  AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"));
  super.onCreate(state);backend=new Backend(this);
  if(backend.signedIn())home();else login();
 }
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private void page(String title){
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xFFF4F7F9);
  body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(20),dp(24),dp(20),dp(24));
  scroll.addView(body);setContentView(scroll);
  androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(scroll,(v,insets)->{
   androidx.core.graphics.Insets safe=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()|androidx.core.view.WindowInsetsCompat.Type.ime());
   v.setPadding(safe.left,safe.top,safe.right,safe.bottom);return insets;
  });
  text(body,title,26).setTypeface(null,Typeface.BOLD);
 }
 private TextView text(LinearLayout box,String value,int size){
  TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(0xFF1C2A37);view.setPadding(0,dp(8),0,dp(8));box.addView(view);return view;
 }
 private Button button(LinearLayout box,String label,Runnable action){
  Button view=new Button(this);view.setText(label);view.setAllCaps(false);view.setMinHeight(dp(48));box.addView(view,new LinearLayout.LayoutParams(-1,-2));view.setOnClickListener(v->{if(!busy)action.run();});return view;
 }
 private EditText input(String label,int type){
  text(body,label,15);EditText e=new EditText(this);e.setInputType(type);e.setSingleLine();e.setMinHeight(dp(48));body.addView(e,new LinearLayout.LayoutParams(-1,-2));return e;
 }
 private void error(String message){new AlertDialog.Builder(this).setTitle("Mi tiempo de estudio").setMessage(message).setPositiveButton("Entendido",null).show();}
 private void login(){
  screen="login";backend.stop();all.clear();page("Mi tiempo de estudio");
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.ic_logo);logo.setContentDescription("Logo de la aplicaciÃ³n");body.addView(logo,new LinearLayout.LayoutParams(dp(88),dp(88)));
  text(body,"Registra tus sesiones y conoce cÃ³mo distribuyes tu tiempo.",18);
  button(body,"Iniciar sesiÃ³n Â· Registrarme Â· Recuperar contraseÃ±a",this::startAuth);
  if(!backend.configured())text(body,"Falta conectar esta instalaciÃ³n a Firebase. Consulta CONFIGURAR_FIREBASE.md incluido en el repositorio.",16);
 }
 private void startAuth(){
  if(!backend.configured()){error("Agrega el archivo de tu aplicaciÃ³n Firebase en app/google-services.json y vuelve a compilar.");return;}
  List<AuthUI.IdpConfig> providers=new ArrayList<>();
  providers.add(new AuthUI.IdpConfig.EmailBuilder().build());providers.add(new AuthUI.IdpConfig.GoogleBuilder().build());
  boolean facebookConfigured=!getString(R.string.facebook_application_id).equals("0")&&!getString(R.string.facebook_client_token).equals("CONFIGURAR");
  AuthMethodPickerLayout.Builder picker=new AuthMethodPickerLayout.Builder(facebookConfigured?R.layout.auth_picker_facebook:R.layout.auth_picker).setEmailButtonId(R.id.auth_email).setGoogleButtonId(R.id.auth_google);
  if(facebookConfigured){
   if(!com.facebook.FacebookSdk.isInitialized())com.facebook.FacebookSdk.sdkInitialize(getApplicationContext());
   providers.add(new AuthUI.IdpConfig.FacebookBuilder().build());picker.setFacebookButtonId(R.id.auth_facebook);
  }
  startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).setAuthMethodPickerLayout(picker.build()).setTheme(R.style.AppTheme).setLogo(R.drawable.ic_logo).setIsSmartLockEnabled(false).build(),AUTH);
 }
 @Override protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(request==AUTH){if(result==RESULT_OK&&backend.signedIn())home();else{
   IdpResponse response=IdpResponse.fromResultIntent(data);
   if(response!=null&&response.getError()!=null)error("No se pudo iniciar sesiÃ³n. Comprueba la conexiÃ³n y la configuraciÃ³n del proveedor.");
  }}
 }
 private void home(){
  screen="list";page("Mis sesiones de estudio");text(body,backend.email(),15);
  button(body,"+ Agregar sesiÃ³n",()->editor(null));
  button(body,"Ver grÃ¡ficos",()->{selectedCourse=null;charts();});
  button(body,"Cerrar sesiÃ³n",()->{busy=true;backend.logout(e->{busy=false;if(e!=null)error(e);else login();});});
  content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);body.addView(content);
  text(content,"Cargando sesionesâ€¦",16);
  backend.listen((rows,e)->{
   if(e!=null){all.clear();if(screen.equals("list")){content.removeAllViews();text(content,e,16);button(content,"Reintentar",this::home);}else if(screen.equals("charts"))charts();return;}
   all.clear();all.addAll(rows);
   if(screen.equals("list"))renderList();else if(screen.equals("charts"))charts();
  });
 }
 private void renderList(){
  content.removeAllViews();double total=0;for(Reading r:all)total+=r.hours;
  text(content,all.size()+" sesiones Â· "+number(total)+" horas en total",18);
  if(all.isEmpty())text(content,"AÃºn no tienes sesiones. Agrega la primera para comenzar.",16);
  for(Reading r:all){
   LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(12),dp(16),dp(12));card.setBackgroundColor(Color.WHITE);
   LinearLayout.LayoutParams margins=new LinearLayout.LayoutParams(-1,-2);margins.topMargin=dp(16);content.addView(card,margins);
   text(card,r.course,21).setTypeface(null,Typeface.BOLD);text(card,r.date+" Â· "+number(r.hours)+" horas Â· "+r.activity,16);
   button(card,"Editar",()->editor(r));
   button(card,"Eliminar",()->new AlertDialog.Builder(this).setTitle("Eliminar sesiÃ³n").setMessage("Â¿Eliminar la sesiÃ³n de "+r.course+" del "+r.date+"?").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->{busy=true;backend.delete(r.id,e->{busy=false;if(e!=null)error(e);});}).show());
  }
 }
 private void editor(Reading original){
  screen="editor";page(original==null?"Agregar sesiÃ³n":"Editar sesiÃ³n");
  EditText course=input("Curso",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
  EditText hours=input("Horas dedicadas (mayor que 0, mÃ¡ximo 24)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
  text(body,"Fecha",15);String initial=original==null?LocalDate.now().toString():original.date;
  Button date=button(body,initial,()->{});
  date.setOnClickListener(v->{if(busy)return;LocalDate current=LocalDate.parse(date.getText());new DatePickerDialog(this,(view,y,m,d)->date.setText(LocalDate.of(y,m+1,d).toString()),current.getYear(),current.getMonthValue()-1,current.getDayOfMonth()).show();});
  text(body,"Tipo de actividad",15);Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,Config.ACTIVITIES));body.addView(type);
  if(original!=null){course.setText(original.course);hours.setText(Double.toString(original.hours));type.setSelection(Math.max(0,Arrays.asList(Config.ACTIVITIES).indexOf(original.activity)));}
  button(body,"Guardar",()->{
   Reading row=new Reading();row.id=original==null?"":original.id;row.course=course.getText().toString().trim();row.date=date.getText().toString();row.activity=type.getSelectedItem().toString();
   try{row.hours=Double.parseDouble(hours.getText().toString().trim().replace(',','.'));}catch(Exception e){hours.setError("Ingresa un nÃºmero vÃ¡lido.");return;}
   String invalid=row.validationError();if(invalid!=null){error(invalid);return;}
   busy=true;backend.save(row,e->{busy=false;if(e!=null)error(e);else{Toast.makeText(this,"SesiÃ³n guardada",Toast.LENGTH_SHORT).show();home();}});
  });
  button(body,"Cancelar",this::home);
 }
 private void charts(){
  screen="charts";page("GrÃ¡ficos de estudio");button(body,"Volver a mis sesiones",this::home);
  text(body,"Filtrar ambos grÃ¡ficos por curso",16);
  Set<String> uniqueCourses=new TreeSet<>();for(Reading r:all)uniqueCourses.add(r.course);
  List<String> courses=new ArrayList<>(uniqueCourses);
  if(selectedCourse!=null&&!courses.contains(selectedCourse))selectedCourse=null;
  List<String> labels=new ArrayList<>();labels.add("Todos los cursos");labels.addAll(courses);
  Spinner filter=new Spinner(this);filter.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));
  filter.setSelection(selectedCourse==null?0:courses.indexOf(selectedCourse)+1);body.addView(filter);
  LinearLayout chartBox=new LinearLayout(this);chartBox.setOrientation(LinearLayout.VERTICAL);body.addView(chartBox);
  Runnable draw=()->{
   chartBox.removeAllViews();Map<String,Double> months=StudyStats.months(all,selectedCourse),activities=StudyStats.activities(all,selectedCourse);
   text(chartBox,"Horas de estudio por mes",21);
   HorizontalScrollView horizontal=new HorizontalScrollView(this);horizontal.setFillViewport(true);
   ChartView bars=new ChartView(this,months,true);horizontal.addView(bars,new android.widget.FrameLayout.LayoutParams(Math.max(getResources().getDisplayMetrics().widthPixels-dp(40),dp(78*months.size()+64)),dp(280)));chartBox.addView(horizontal);
   text(chartBox,"ProporciÃ³n de horas por actividad",21);chartBox.addView(new ChartView(this,activities,false),new LinearLayout.LayoutParams(-1,dp(240)));
   double total=0;for(double h:activities.values())total+=h;
   if(total==0)text(chartBox,"No hay sesiones para este filtro.",16);
   for(Map.Entry<String,Double> entry:activities.entrySet())text(chartBox,entry.getKey()+": "+number(entry.getValue())+" h ("+number(total==0?0:100*entry.getValue()/total)+" %)",16);
   for(Map.Entry<String,Double> entry:months.entrySet())text(chartBox,entry.getKey()+": "+number(entry.getValue())+" horas",14);
  };
  filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
   public void onItemSelected(android.widget.AdapterView<?> p,View v,int position,long id){selectedCourse=position==0?null:courses.get(position-1);draw.run();}
   public void onNothingSelected(android.widget.AdapterView<?> p){}
  });draw.run();
 }
 private String number(double n){return String.format(new Locale("es","PE"),"%.2f",n);}
 @Override public void onBackPressed(){if(busy)return;if(screen.equals("editor")||screen.equals("charts"))home();else super.onBackPressed();}
 @Override protected void onDestroy(){backend.stop();super.onDestroy();}
}
