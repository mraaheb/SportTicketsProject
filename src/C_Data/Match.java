package C_Data;
public class Match {
  public final String id,event,date,time,desc; public int available;
  public Match(String id,String e,String d,String t,String desc,int av){
    this.id=id.trim(); this.event=e.trim(); this.date=d.trim();
    this.time=t.trim();
    this.desc=desc.trim(); this.available=av;
  }
  public String toCSV()
  { return String.join(",",id,event,date,time,desc,String.valueOf(available));
  }
}
