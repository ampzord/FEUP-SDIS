import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MulticastSocketServer {
    
    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8888;

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        
    	/*if (!validArgumentNumber(args)) {
    		return;
    	}*/
    	
    	
    	// Get the address that we are going to connect to.
        InetAddress addr = InetAddress.getByName(INET_ADDR);
     
        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket serverSocket = new DatagramSocket()) {
        	for (int i = 0; i < 5; i++) {
                String msg = "Sent message no " + i;

                // Create a packet that will contain the data
                // (in the form of bytes) and send it.
                DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr, PORT);
                serverSocket.send(msgPacket);
     
                System.out.println("Server sent packet with msg: " + msg);
                Thread.sleep(500);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /*
     *  <mcast_addr> is the IP address of the multicast group used by the server to advertise its service;
	 *	<mcast_port> is the port number of the multicast group used by the server to advertise its service;
		<oper> is ''register'' or ''lookup'', depending on the operation to invoke;
		<opnd> * is the list of operands of the specified operation:
		<plate number> <owner name>, for register;
		<plate number>, for lookup.
     */
    private static boolean validArgumentNumber(String[] args) {
    	if (args.length != 3) {
    		System.out.println("multicast: <mcast_addr> <mcast_port>: <srvc_addr> <srvc_port> ");
    		return false;
    	}
    	return true;
    }
    
    private static void parseArguments(String[] args) {
    	//INET_ADDR = args[0];
    	//PORT = Integer.parseInt(args[1]);
    	
    	
    	System.out.println(INET_ADDR);
    	System.out.println(PORT);
    }
}