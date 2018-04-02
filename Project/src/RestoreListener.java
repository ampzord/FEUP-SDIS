package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
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
	
    private Hashtable<Integer,byte[]> chunks = new Hashtable<Integer,byte[]>();
    
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
	            byte[] buf = new byte[256];
	
	            DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            MDR.receive(packet);
	            String request = new String(buf, 0, buf.length, Charset.forName("ISO_8859_1"));
	            request = request.trim();
	            String[] data = request.split(" ");
	            //Print request if it's from a different peer
	            if(server.files.get(data[3]) != null)
	            	System.out.println("Peer " + server.ID + ": received request - " + request);
	            else {
	            	continue;
	            }
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
        String data = request[5];
        int chunkNo = Integer.parseInt(request[4]);
        if(!data.contains(server.CRLF+server.CRLF)){
        	System.out.println("	Invalid flags");
        	return;
        }
        
        if(operation.compareTo("CHUNK") == 0){
            //Broadcast protocol to use
            System.out.println("Peer " + server.ID + ": starting CHUNK protocol");
            
            //Add chunk to Hashtable if it hasn't already
            String body = data.substring(8);
            
            //Broadcast end of protocol
    		System.out.println("Peer "+server.ID+": finished CHUNK protocol");
            
            if(getChunks().get(chunkNo) == null)
            	getChunks().put(chunkNo, body.getBytes());
            //When duplicate chunk is received, ignore it
            else
            	return;
        }
	}

	public Hashtable<Integer,byte[]> getChunks() {
		return chunks;
	}

	public void setChunks(Hashtable<Integer,byte[]> chunks) {
		this.chunks = chunks;
	}
}
