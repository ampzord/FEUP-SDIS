package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class BackupListener extends Listener{

	//Backup Channel
    protected MulticastSocket MDB;
    protected InetAddress MDB_address;
    protected Integer MDB_port;
    
    //Control Channel
    protected DatagramSocket MC;
    protected InetAddress MC_address;
    protected Integer MC_port;
	
	public BackupListener(Server server, String MC_address, Integer MC_port, String MDB_address, Integer MDB_port) throws IOException, UnknownHostException {
		super(server);
		
		this.MC_address = InetAddress.getByName(MC_address);
		this.MC_port = MC_port;
		MC = new DatagramSocket();
		
		MDB = new MulticastSocket(MDB_port);
        this.MDB_address = InetAddress.getByName(MDB_address);
        this.MDB_port = MDB_port;
        MDB.joinGroup(this.MDB_address);
	}

	@Override
    public void run() {
		System.out.println("Peer "+server.ID+": BackupListener Online!");
		
        while(true) {
            try {
                //Retrieve packet from the MDB channel
	            byte[] buf = new byte[65000];
	
	            DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            MDB.receive(packet);
	            String request = new String(packet.getData(), StandardCharsets.ISO_8859_1);
	            //Clean unused bytes
	            request = request.trim();
	            //Clear flag bytes used (CRLF)
	            request = request.substring(0, request.length()-4);
	            //Print request if it's from a different peer
	            if(request.contains(server.ID))
	            	continue;
	            
	            //Analize request & execute protocol
	            try {
                    protocol(request.split(" "));
                }catch(NoSuchAlgorithmException | InterruptedException e){
                    System.out.println("NoSuchAlgorithmException caught in Server thread");
                }
            }catch(IOException e){
            	System.out.println(e.toString());
            }
	    }
	}
	
	private void protocol(String[] request) throws NoSuchAlgorithmException, IOException, InterruptedException{
		String operation = request[0];
		String version = request[1];
        String senderId = request[2];
        String fileId = request[3];
        int chunkNo = Integer.parseInt(request[4]);
        String replicationDeg = request[5];
        
        System.out.println("Peer "+server.ID+": received request - "+operation+" "+version+" "+senderId+" "+fileId+" "+chunkNo+" "+replicationDeg);
        
        if(!request[6].contains(server.CRLF+server.CRLF)){
        	System.out.println("Invalid flags");
        	return;
        }
        String body = request[6].substring(8);
        
        for(int i = 7; i < request.length; i++)
        	body = body + " " + request[i];
        
        if(request[0].compareTo("PUTCHUNK") == 0){

            //Broadcast protocol to use
            System.out.println("Peer "+server.ID+": starting PUTCHUNK protocol");
            
            Path filePath = Paths.get("src/Chunks/"+fileId+"/"+chunkNo);
            
            Files.write(filePath, body.getBytes(StandardCharsets.ISO_8859_1));
            
            //Broadcast after random delay
    		Random rand = new Random();
    		int delay = rand.nextInt(400);
    		Thread.sleep(delay);
    		
    		String msg = "STORED "+version+" "+server.ID+" "+fileId+" "+chunkNo+" "+server.CRLF+server.CRLF;
    		
    		DatagramPacket packet = new DatagramPacket(msg.getBytes(StandardCharsets.ISO_8859_1), msg.length(), MC_address, MC_port);
            MC.send(packet);
            
            //Broadcast end of protocol
    		System.out.println("Peer "+server.ID+": finished PUTCHUNK protocol");
        }
	}
}
