package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ControlListener extends Listener {

	//Control Channel
    protected MulticastSocket MC;
    protected InetAddress MC_address;
    protected Integer MC_port;
    
	public ControlListener(Server server, String MC_address, Integer MC_port) throws IOException {
		super(server);
		
		MC = new MulticastSocket(MC_port);
        this.MC_address = InetAddress.getByName(MC_address);
        this.MC_port = MC_port;
        MC.joinGroup(this.MC_address);
	}
	
	@Override
    public void run() {
		System.out.println("Peer "+server.ID+": ControlListener Online!");
		
        while(true) {
            try {
                //Retrieve packet from the MC channel
	            byte[] buf = new byte[256];
	
	            DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            MC.receive(packet);
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
