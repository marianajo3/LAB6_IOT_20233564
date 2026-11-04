package pe.lab6.ecohuerto;
import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.util.*;
public class MainActivity extends Activity  {
    private Gateway backend;
    private LinearLayout body,records,graphs;
    private TextView stats,status;
    private java.util.List<Reading> all=new ArrayList<>();
    private String selected="Todas",since="",until="";
    private boolean busy=false,dashboard=false;
    private String screen="login";
    private int accent=Color.parseColor(Config.COLOR),ink=Color.rgb(28,42,55);
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        backend=new Backend(this);
        getWindow().setStatusBarColor(accent);
        getWindow().setNavigationBarColor(Color.WHITE);
        if(backend.signedIn())home();
        else auth("login");
    }
    private int dp(int n) {
        return Math.round(n*getResources().getDisplayMetrics().density);
    }
    private GradientDrawable background(int color,int radius) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }
    private void page() {
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        // Android 15+ aplica edge-to-edge: respeta barras del sistema y teclado.
        if (Build.VERSION.SDK_INT >= 30) {
            scroll.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
                return insets;
            });
        }
        scroll.setBackgroundColor(0xFFF4F7F9);
        body=new LinearLayout(this);
        body.setOrientation(1);
        body.setPadding(dp(22),dp(22),dp(22),dp(28));
        scroll.addView(body);
        setContentView(scroll);
    }
    private TextView label(LinearLayout box,String text,int size,int color) {
        TextView t=new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(0,dp(6),0,dp(6));
        box.addView(t);
        return t;
    }
    private void title(LinearLayout box,String text,int size) {
        label(box,text,size,ink).setTypeface(null,Typeface.BOLD);
    }
    private LinearLayout card(LinearLayout parent) {
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(1);
        box.setPadding(dp(16),dp(12),dp(16),dp(12));
        box.setBackground(background(Color.WHITE,16));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(12),0,dp(4));
        parent.addView(box,p);
        return box;
    }
    private Button button(LinearLayout parent,String text,boolean primary,Runnable action) {
        Button b=new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTextColor(primary?Color.WHITE:accent);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary?accent:Color.parseColor(Config.LIGHT)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.topMargin=dp(7);
        parent.addView(b,p);
        b.setOnClickListener(v-> {
            if(!busy)action.run();
        }
        );
        return b;
    }
    private EditText field(LinearLayout parent,String name,int type) {
        label(parent,name,13,ink);
        EditText e=new EditText(this);
        e.setInputType(type);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setPadding(dp(10),dp(9),dp(10),dp(9));
        e.setBackground(background(0xFFF1F4F6,8));
        e.setMinHeight(dp(48));
        parent.addView(e,new LinearLayout.LayoutParams(-1,-2));
        return e;
    }
    private void show(String text) {
        new AlertDialog.Builder(this).setTitle(Config.NAME).setMessage(text).setPositiveButton("Entendido",null).show();
    }
    private void working(boolean value) {
        busy=value;
        if(status!=null)status.setText(value?"Procesando…":backend.mode());
    }
    private Gateway.Done authResult() {
        return error-> {
            working(false);
            if(error!=null)show(error);
            else home();
        }
        ;
    }
    private boolean validEmail(EditText email) {
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
            email.setError("Ingresa un correo válido");
            return false;
        }
        return true;
    }
    private void auth(String target) {
        backend.stop();
        dashboard=false;
        screen=target;
        page();
        ImageView logo=new ImageView(this);
        logo.setContentDescription("Logo de "+Config.NAME);
        body.addView(logo,new LinearLayout.LayoutParams(dp(70),dp(70)));
        title(body,Config.NAME,31);
        label(body,Config.SUBTITLE,16,0xFF65717E);
        status=label(body,backend.mode(),12,accent);
        LinearLayout form=card(body);
        title(form,target.equals("register")?"Crea tu cuenta":target.equals("reset")?"Recupera tu acceso":"Bienvenido",23);
        EditText email=field(form,"Correo electrónico",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText password=target.equals("reset")?null:field(form,"Contraseña",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm=target.equals("register")?field(form,"Confirma tu contraseña",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD):null;
        if(target.equals("reset")) {
            label(form,"Te enviaremos un enlace para establecer una nueva contraseña.",14,0xFF65717E);
            button(form,"Enviar enlace",true,()-> {
                if(!validEmail(email))return; working(true); backend.reset(email.getText().toString().trim(),err-> {
                    working(false); show(err==null?"Si hay una cuenta asociada a este correo, recibirás las instrucciones. Revisa también spam.":err);
                }
                );
            }
            );
        }
        else button(form,target.equals("register")?"Registrarme":"Iniciar sesión",true,()-> {
            if(!validEmail(email))return; String pass=password.getText().toString(); if(pass.length()<6) {
                password.setError("Usa al menos 6 caracteres"); return;
            }
            if(confirm!=null&&!pass.equals(confirm.getText().toString())) {
                confirm.setError("Las contraseñas no coinciden"); return;
            }
            working(true); if(confirm==null)backend.login(email.getText().toString().trim(),pass,authResult()); else backend.register(email.getText().toString().trim(),pass,authResult());
        }
        );
        if(target.equals("login")) {
            button(form,"Continuar con Google",false,()-> {
                working(true); backend.provider("Google",this,authResult());
            }
            );
            button(body,"Crear una cuenta",false,()->auth("register"));
            button(body,"Olvidé mi contraseña",false,()->auth("reset"));
            if(!BuildConfig.CLOUD) {
                button(body,"Entrar a la demo",true,()->backend.guest(authResult()));
                label(body,"La demo guarda datos en este dispositivo. Google, Facebook, correos y recuperación requieren la versión Firebase.",13,0xFF65717E);
            }
        }
        else button(body,"Volver a iniciar sesión",false,()->auth("login"));
    }
    private void refresh() {
        if(records==null||graphs==null||!dashboard)return;
        java.util.List<Reading> rows=filtered();
        double sum=0;
        for(Reading r:rows)sum+=r.value;
        stats.setText(rows.isEmpty()?"Sin mediciones para este filtro":rows.size()+" registros · Promedio "+format(sum/rows.size())+" "+Config.UNIT);
        graphs.removeAllViews();
        LinearLayout line=card(graphs);
        title(line,"Evolución de "+Config.METRIC.toLowerCase(Locale.ROOT),18);
        line.addView(new ChartView(this,rows,false),new LinearLayout.LayoutParams(-1,dp(240)));
        LinearLayout bars=card(graphs);
        records.removeAllViews();
        title(records,"Tus mediciones",21);
        if(rows.isEmpty())label(records,"Añade una medición o cambia el filtro para comenzar.",15,0xFF65717E);
        for(int i=rows.size()-1; i>=0; i--) {
            Reading r=rows.get(i);
            LinearLayout card=card(records);
            title(card,format(r.value)+" "+Config.UNIT,24);
            label(card,r.group+" · "+r.device+"\n"+r.date,14,0xFF65717E);
            if(!r.notes.isEmpty())label(card,r.notes,14,ink);
            button(card,"Editar",false,()->editor(r));
            button(card,"Eliminar",false,()->new AlertDialog.Builder(this).setTitle("Eliminar medición").setMessage("¿Eliminar el registro de "+r.device+" del "+r.date+"?").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)-> {
                        working(true); backend.delete(r.id,error-> {
                                    working(false); if(error!=null)show(error);
                                }
                        );
                    }
            ).show());
        }
    }
    private void editor(Reading original) {
        LinearLayout box=new LinearLayout(this);
        box.setPadding(dp(20),dp(8),dp(20),dp(8));
        box.setOrientation(1);
        EditText device=field(box,Config.DEVICE,InputType.TYPE_CLASS_TEXT);
        label(box,Config.GROUP,13,ink);
        Spinner group=new Spinner(this);
        group.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,Config.GROUPS));
        box.addView(group);
        EditText value=field(box,Config.METRIC+" ("+Config.UNIT+")",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|(Config.MIN<0?InputType.TYPE_NUMBER_FLAG_SIGNED:0));
        Button date=button(box,original==null?LocalDate.now().toString():original.date,false,()-> {
                }
        );
        date.setOnClickListener(v->pickDate(date.getText().toString(),date::setText));
        EditText notes=field(box,"Observaciones (opcional)",InputType.TYPE_CLASS_TEXT);
        if(original!=null) {
            device.setText(original.device);
            group.setSelection(Math.max(0,Arrays.asList(Config.GROUPS).indexOf(original.group)));
            value.setText(String.valueOf(original.value));
            notes.setText(original.notes);
        }
        ScrollView scroll=new ScrollView(this);
        scroll.addView(box);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(original==null?"Nueva "+Config.ENTITY.toLowerCase(Locale.ROOT):"Editar medición").setView(scroll).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w-> {
                    if(busy)return; String dev=device.getText().toString().trim(),note=notes.getText().toString().trim(); if(dev.isEmpty()||dev.length()>60) {
                        device.setError("Escribe entre 1 y 60 caracteres"); return;
                    }
                    if(note.length()>250) {
                        notes.setError("Máximo 250 caracteres"); return;
                    }
                    double number; try {
                        number=Double.parseDouble(value.getText().toString().trim().replace(',','.'));
                    }
                    catch(Exception e) {
                        value.setError("Ingresa un número válido"); return;
                    }
                    if(!Double.isFinite(number)||number<Config.MIN||number>Config.MAX||(Config.INTEGER&&number!=Math.rint(number))) {
                        value.setError("Valor "+(Config.INTEGER?"entero ":"")+"entre "+Config.MIN+" y "+Config.MAX); return;
                    }
                    Reading r=new Reading(); r.id=original==null?"":original.id; r.device=dev; r.group=group.getSelectedItem().toString(); r.value=number; r.date=date.getText().toString(); r.notes=note; working(true); dialog.getButton(-1).setEnabled(false); backend.save(r,error-> {
                                working(false); if(isFinishing())return; dialog.getButton(-1).setEnabled(true); if(error!=null)show(error); else {
                                    dialog.dismiss(); if(r.value>Config.THRESHOLD)notifyThreshold(r);
                                }
                            }
                    );
                }
        ));
        dialog.show();
    }
    private void home() {
        dashboard=true;
        screen="home";
        page();
        title(body,Config.NAME,29);
        label(body,backend.email(),13,0xFF65717E);
        status=label(body,backend.mode(),12,accent);
        stats=label(body,"Cargando registros…",16,ink);
        LinearLayout filters=card(body);
        title(filters,"Filtra tus datos",18);
        label(filters,Config.GROUP,13,ink);
        Spinner group=new Spinner(this);
        java.util.List<String> choices=new ArrayList<>();
        choices.add("Todas");
        choices.addAll(Arrays.asList(Config.GROUPS));
        group.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,choices));
        group.setSelection(Math.max(0,choices.indexOf(selected)));
        filters.addView(group);
        group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p,View v,int pos,long id) {
                selected=choices.get(pos); refresh();
            }
            public void onNothingSelected(AdapterView<?> p) {
            }
        }
        );
        Button start=button(filters,since.isEmpty()?"Desde: todas las fechas":"Desde: "+since,false,()-> {
        }
        );
        start.setOnClickListener(v->pickDate(since,date-> {
            if(!until.isEmpty()&&date.compareTo(until)>0) {
                show("La fecha inicial debe ser anterior o igual a la final."); return;
            }
            since=date; start.setText("Desde: "+date); refresh();
        }
        ));
        Button end=button(filters,until.isEmpty()?"Hasta: todas las fechas":"Hasta: "+until,false,()-> {
        }
        );
        end.setOnClickListener(v->pickDate(until,date-> {
            if(!since.isEmpty()&&date.compareTo(since)<0) {
                show("La fecha final debe ser posterior o igual a la inicial."); return;
            }
            until=date; end.setText("Hasta: "+date); refresh();
        }
        ));
        button(filters,"Limpiar filtros",false,()-> {
            selected="Todas"; since=""; until=""; home();
        }
        );
        button(body,"+ Nuevo curso",true,()->editor(null));
        button(body,"Cargar 6 datos de práctica",false,()->new AlertDialog.Builder(this).setTitle("Añadir datos de ejemplo").setMessage("Se crearán seis registros nuevos en tu almacenamiento actual. Puedes eliminarlos después.").setPositiveButton("Añadir",(d,w)->seed()).setNegativeButton("Cancelar",null).show());
        graphs=new LinearLayout(this);
        graphs.setOrientation(1);
        body.addView(graphs);
        records=new LinearLayout(this);
        records.setOrientation(1);
        body.addView(records);
        button(body,"Activar notificaciones",false,()-> {
            if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[] {
                Manifest.permission.POST_NOTIFICATIONS
            }
            ,90); else show("Notificaciones habilitadas si el canal está permitido en Ajustes. Se generan al guardar mediciones sobre el umbral.");
        }
        );
        button(body,"Cerrar sesión",false,()-> {
            working(true); backend.logout(err-> {
                working(false); if(err!=null)show(err); else {
                    all.clear(); selected="Todas"; since=""; until=""; auth("login");
                }
            }
            );
        }
        );
        backend.listen((items,error)-> {
            if(!dashboard)return; if(error!=null) {
                status.setText(error); all.clear(); refresh(); return;
            }
            all=items; refresh();
        }
        );
    }
    private java.util.List<Reading> filtered() {
        java.util.List<Reading> rows=new ArrayList<>();
        for(Reading r:all)if((selected.equals("Todas")||selected.equals(r.group))&&(since.isEmpty()||r.date.compareTo(since)>=0)&&(until.isEmpty()||r.date.compareTo(until)<=0))rows.add(r);
        rows.sort(Comparator.comparing((Reading r)->r.date).thenComparing(r->r.id));
        return rows;
    }
    private String format(double n) {
        return String.format(new Locale("es","PE"),Config.INTEGER?"%.0f":"%.1f",n);
    }
    interface DateDone {
        void set(String date);
    }
    private void pickDate(String existing,DateDone done) {
        LocalDate now;
        try {
            now=LocalDate.parse(existing);
        }
        catch(Exception e) {
            now=LocalDate.now();
        }
        new DatePickerDialog(this,(v,y,m,d)->done.set(LocalDate.of(y,m+1,d).toString()),now.getYear(),now.getMonthValue()-1,now.getDayOfMonth()).show();
    }
    private void notifyThreshold(Reading r) {
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Medición guardada. Umbral superado; activa las notificaciones para recibir avisos.",Toast.LENGTH_LONG).show();
            return;
        }
        NotificationManager manager=getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel("threshold","Alertas de medición",NotificationManager.IMPORTANCE_DEFAULT));
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pending=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification n=new Notification.Builder(this,"threshold").setSmallIcon(R.drawable.ic_notification).setContentTitle(Config.NAME+": umbral superado").setContentText(r.group+" · "+format(r.value)+" "+Config.UNIT).setContentIntent(pending).setAutoCancel(true).build();
        manager.notify(r.id.hashCode(),n);
    }
    private void seed() {
        working(true);
        seedNext(0);
    }
    private void seedNext(int i) {
        if(i>=Config.EXAMPLES.length) {
            working(false);
            return;
        }
        Reading r=new Reading();
        r.device=Config.DEVICE+"-0"+(i%3+1);
        r.group=Config.GROUPS[i%3];
        r.date=LocalDate.now().minusDays(5-i).toString();
        r.notes=Config.EXAMPLES[i];
        backend.save(r,error-> {
            if(error!=null) {
                working(false); show(error);
            }
            else seedNext(i+1);
        }
        );
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        backend.onActivityResult(request,result,data);
    }
    @Override public void onBackPressed() {
        if(!dashboard&&!screen.equals("login"))auth("login");
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        backend.stop();
        super.onDestroy();
    }
}
