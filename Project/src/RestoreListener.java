package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class RestoreListener extends Listener{

	//Restore Channel
    protected MulticastSocket MDR;
    protected InetAddress MDR_address;
    protected Integer MDR_port;
    
    //Control Channel
    protected DatagramSocket MC;
    protected InetAddress MC_address;
    protected Integer MC_port;
	
	public RestoreListener(Server server, String MC_address, Integer MC_port, String MDR_address, Integer MDR_port) throws IOException, UnknownHostException, IOException {
		super(server);
		
		this.MC_address = InetAddress.getByName(MC_address);
		this.MC_port = MC_port;
		MC = new DatagramSocket();
		
		MDR = new MulticastSocket(MDR_port);
        this.MDR_address = InetAddress.getByName(MDR_address);
        this.MDR_port = MDR_port;
        MDR.joinGroup(this.MDR_address);
	}

	@Override
    public void run() {
		System.out.println("Peer "+server.ID+": ControlListener Online!");
		
        while(true) {
            try {
                //Retrieve packet from the MDR channel
	            byte[] buf = new byte[256];
	
	            DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            MDR.receive(packet);
	            String request = new String(buf, 0, buf.length);
	            request = request.trim();
	            //Print request & continue IF fileId is in this peer's system
	            if(server.files.get(request.split(" ")[3]) != null) {
	            	System.out.println("Peer "+server.ID+": received request - "+request);
	            }
	            else {
	            	continue;
	            }
	            protocol(request.split(" "));
            }catch(IOException e){
            	System.out.println("IOException caught in Server thread");
            }
	    }
	}

	private void protocol(String[] request) throws IOException {
		String operation = request[0];
		if(operation.compareTo("STORED") == 0) {
			String fileId = request[3];
			Integer replicationDeg = server.files.get(fileId).getReplicationDeg();
            server.files.get(fileId).setReplicationDeg(replicationDeg+1);
		}
	}
}
