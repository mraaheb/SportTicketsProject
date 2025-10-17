package B_Server;
import java.net.*; public class NewServer {
  public static final int PORT=9090;
  public static void main(String[] args) throws Exception{
    try(
            ServerSocket ss=new ServerSocket(PORT)){
      System.out.println("Server listening on "+PORT+" ...");
       
      while(true)
      { Socket c=ss.accept(); 
      new Thread(new ClientHandler(c)).start(); }
    }
    
  }


}

