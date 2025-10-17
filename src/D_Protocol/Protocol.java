package D_Protocol;
import C_Data.Match; import java.util.*;
public class Protocol {
  public static final String REGISTER="REGISTER", LOGIN="LOGIN", LIST="LIST", RESERVE="RESERVE";
  public static final String OK="OK", ERROR="ERROR", CONFIRMED="CONFIRMED", SOLD_OUT="SOLD_OUT";
  public static String makeRegister(String u,String p)
  { return REGISTER+","+u+","+p; }
  public static String makeLogin(String u,String p)
  { return LOGIN+","+u+","+p; }
  public static String makeList(String e,String d)
  { return LIST+","+e+","+d; }
  public static String makeReserve(String id,int c)
  { return RESERVE+","+id+","+c; }
  public static String matchToLine(Match m
  ){ return m.id+"|"+m.time+"|"+m.desc+"|"+m.available; }
  public static List<String[]> parseListResponse(String raw){
    List<String[]> rows=new ArrayList<>(); 
    if(raw==null||raw.isBlank()) 
        return rows;
    for(String ln: raw.split("\\r?\\n")){
        String[] p=ln.split("\\|",-1); if(p.length>=4) rows.add(p); } 
    return rows; }
}

