package pe.lab6.ecohuerto;
// Cambia este archivo para adaptar la app al enunciado que recibas.
public final class Config  {
    public static final String NAME="Notas",SUBTITLE="Revisa tus notas",ENTITY="Lectura ",GROUP="Zona",DEVICE="CURSO",UNIT=".",METRIC="CURSOS",COLOR="#137A63",LIGHT="#E6F4ED",PRUEBA1 = "IOT", PRUEBA2  = "REDES";
    public static final String[] GROUPS= {
        "REDES","WEB","FISICA"
    }
    ;
    public static final double MIN=-20,MAX=80,THRESHOLD=30;
    public static final boolean INTEGER=false;
    public static final String [] EXAMPLES= {
        "Servicios y Aplicaciones para IOT","Comunicaciones digitales", "Redes de Computadoras","Servicios y Aplicaciones para IOT","Comunicaciones digitales"
    }
    ;
    private Config() {
    }
}
