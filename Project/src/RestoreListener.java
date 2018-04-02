package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

public class RestoreListener extends Listener{

	//Restore Channel
    protected MulticastSocket MDR;
    protected InetAddress MDR_address;
    protected Integer MDR_port;
    
    //Control Channel
    protected DatagramSocket MC;
    protected InetAddress MC_address;
    protected Integer MC_port;
	
    private Hashtable<Integer,String> chunks = new Hashtable<Integer,String>();
    
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
		System.out.println("Peer " + server.ID + ": RestoreListener Online!");
		
        while(true) {
            try {
                //Retrieve packet from the MDR channel
	            byte[] buf = new byte[65000];
	
	            DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            MDR.receive(packet);
	            String request = new String(buf, 0, buf.length, StandardCharsets.ISO_8859_1);
	            //Clean unused bytes
	            request = request.trim();
	            //Clear flag bytes used (CRLF)
	            request = request.substring(0, request.length()-4);
	            //Split header
	            String[] data = request.split(" ");
	            //Print request if it's from a different peer
	            if(server.files.get(data[3]) == null)
	            	continue;
	            
	            //Analize request & execute protocol
	            try {
                    protocol(data);
                }catch(NoSuchAlgorithmException | InterruptedException e){
                    System.out.println("NoSuchAlgorithmException caught in Server thread");
                }
            }catch(IOException e){
            	System.out.println("IOException caught in Server thread");
            }
	    }
	}
	
	private void protocol(String[] request) throws NoSuchAlgorithmException, IOException, InterruptedException{
		String operation = request[0];
        String version = request[1];
        String senderId = request[2];
        String fileId = request[3];
        int chunkNo = Integer.parseInt(request[4]);
        
        System.out.println("Peer "+server.ID+": received request - "+operation+" "+version+" "+senderId+" "+fileId+" "+chunkNo);
        
        if(!request[5].contains(server.CRLF+server.CRLF)){
        	System.out.println("	Invalid flags");
        	return;
        }
        String body = request[5].substring(8);
        
        for(int i = 6; i < request.length; i++)
        	body += " " + request[i];
        
        if(operation.compareTo("CHUNK") == 0){
            //Broadcast protocol to use
            System.out.println("Peer " + server.ID + ": starting CHUNK protocol");
            
            if(getChunks().get(chunkNo) == null) {
            	getChunks().put(chunkNo, body);

            	//Broadcast end of protocol
        		System.out.println("Peer "+server.ID+": finished CHUNK protocol");
            }
            //When duplicate chunk is received, ignore it
            else {
            	//Broadcast end of protocol
        		System.out.println("Peer "+server.ID+": finished CHUNK protocol");
            	return;
            }
        }
	}

	public Hashtable<Integer, String> getChunks() {
		return chunks;
	}

	public void setChunks(Hashtable<Integer, String> chunks) {
		this.chunks = chunks;
	}
}
