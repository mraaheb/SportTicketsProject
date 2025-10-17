package E_ClientConn;
import D_Protocol.Protocol;
public class ClientTest {
  public static void main(String[] a) throws Exception {
    ClientConnection c=new ClientConnection(); 
    if(!c.connect("localhost",9090))
    { System.out.println("Cannot connect");
    return; }
    
    /*System.out.println(c.sendOneLine(Protocol.makeRegister("mai","1234")));
    System.out.println(c.sendOneLine(Protocol.makeLogin("mai","1234")));
    System.out.println(c.sendMultiLine(Protocol.makeList("football","2025-29-10")));
    System.out.println(c.sendOneLine(Protocol.makeReserve("M001",1)));
    //System.out.println(c.sendAndReceiveLine(Protocol.makeReserve("M003", 1)));
*/
    System.out.println("== MATCHES,football ==");
        System.out.println(c.sendMultiLine("MATCHES,football")); // يطلع قائمة أوصاف المباريات المتاحة

        System.out.println("== DATES2,football,AlHilal vs AlNassr ==");
        System.out.println(c.sendMultiLine("DATES2,football,AlHilal vs AlNassr")); // تواريخ متاحة لها

        System.out.println("== LIST,football,2025-10-29 ==");
        System.out.println(c.sendMultiLine("LIST,football,2025-10-29")); // أسطر: id | time | desc | left:N

        System.out.println("== RESERVE,M001,2 ==");
        System.out.println(c.sendOneLine("RESERVE,M001,2")); // CONFIRMED,...
    /*System.out.println("== MATCHES,football ==");
System.out.println(c.sendMultiLine("MATCHES,football"));

System.out.println("== DATES2,football,AlHilal vs AlNassr ==");
System.out.println(c.sendMultiLine("DATES2,football,AlHilal vs AlNassr"));

System.out.println("== LIST,football,10/29/2025 ==");
System.out.println(c.sendMultiLine("LIST,football,10/29/2025"));*/

    c.close();
  }
}
