
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastSocketClient {
	
	// 224.0.0.0 to 239.255.255.255
    
    //final static String INET_ADDR = "224.0.0.3";
    //final static int PORT = 8888;

	static String INET_ADDR;
	static int PORT;

    public static void main(String[] args) throws UnknownHostException {
    	
    	/*if (!validArgumentNumber(args)) {
    		return;
    	}*/
    	
    	parseArguments(args);
    	
        // Get the address that we are going to connect to.
        InetAddress address = InetAddress.getByName(INET_ADDR);
        
        // Create a buffer of bytes, which will be used to store
        // the incoming bytes containing the information from the server.
        // Since the message is small here, 256 bytes should be enough.
        byte[] buf = new byte[256];
        
        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        try (MulticastSocket clientSocket = new MulticastSocket(PORT)){
            
        	//Joint the Multicast group.
            clientSocket.joinGroup(address);
     
            while (true) {
            	
                // Receive the information and print it.
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);

                String msg = new String(buf, 0, buf.length);
                System.out.println("Socket 1 received msg: " + msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private static boolean validArgumentNumber(String[] args) {
    	if (args.length != 3) {
    		System.out.println("USAGE: java client <mcast_addr> <mcast_port> <oper> <opnd>*");
    		return false;
    	}
    	return true;
    }
    
    private static void parseArguments(String[] args) {
    	INET_ADDR = args[0];
    	PORT = Integer.parseInt(args[1]);
    	
    	
    	System.out.println(INET_ADDR);
    	System.out.println(PORT);
    }
}
