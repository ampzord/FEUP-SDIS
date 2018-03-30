package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
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
	
	public BackupListener(String ID, String MC_address, Integer MC_port, String MDB_address, Integer MDB_port) throws IOException, UnknownHostException, IOException {
		super(ID);
		
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
		System.out.println("Peer "+ID+": BackupListener Online!");
		
        while(true) {
            try {
                //Retrieve packet from the MDB channel
	            byte[] buf = new byte[256];
	
	            DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            MDB.receive(packet);
	            String request = new String(buf, 0, buf.length);
	            request = request.trim();
	            //Print request if it's from a different peer
	            if(!request.contains(ID))
	            	System.out.println("Peer "+ID+": received request - "+request);
	            else {
	            	continue;
	            }
	            //Analize request & execute protocol
	            try {
                    protocol(request.split(" "));
                }catch(NoSuchAlgorithmException | InterruptedException e){
                    System.out.println("NoSuchAlgorithmException caught in Server thread");
                }
            }catch(IOException e){
            	System.out.println("IOException caught in Server thread");
            }
	    }
	}
	
	private void protocol(String[] request) throws NoSuchAlgorithmException, IOException, InterruptedException{
        String version = request[1];
        String fileId = request[3];
        int chunkNo = Integer.parseInt(request[4]);
        if(!request[6].contains(CRLF+CRLF)){
        	System.out.println("Invalid flags");
        	return;
        }
        String body = request[6].substring(4);
        
        if(request[0].compareTo("PUTCHUNK") == 0){

            //Broadcast protocol to use
            System.out.println("Peer "+ID+": starting PUTCHUNK protocol");
            
    		Path file = Paths.get("src/Chunks/"+fileId);
    		Files.write(file, body.getBytes());
    		
    		//Broadcast after random delay
    		Random rand = new Random();
    		int delay = rand.nextInt(400);
    		Thread.sleep(delay);
    		
    		//Broadcast end of protocol
    		System.out.println("Peer "+ID+": finished PUTCHUNK protocol");
    		
    		String msg = "STORED "+version+" "+ID+" "+fileId+" "+chunkNo+" "+CRLF+CRLF;
    		
    		DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), MC_address, MC_port);
            MC.send(packet);
        }
	}
}
