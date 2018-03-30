package src;

import java.io.IOException;
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
	
	public RestoreListener(String ID, String MC_address, Integer MC_port, String MDR_address, Integer MDR_port) throws IOException, UnknownHostException, IOException {
		super(ID);
		
		this.MC_address = InetAddress.getByName(MC_address);
		this.MC_port = MC_port;
		MC = new DatagramSocket();
		
		MDR = new MulticastSocket(MDR_port);
        this.MDR_address = InetAddress.getByName(MDR_address);
        this.MDR_port = MDR_port;
        MDR.joinGroup(this.MDR_address);
	}

}
