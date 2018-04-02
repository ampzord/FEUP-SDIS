package src;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ControlListener extends Listener {

	//Control Channel
    protected MulticastSocket MC;
    protected InetAddress MC_address;
    protected Integer MC_port;
    
    //Restore Channel Info
    protected DatagramSocket MDR;
    protected InetAddress MDR_address;
    protected Integer MDR_port;
    
	public ControlListener(Server server, String MC_address, Integer MC_port, String MDR_address, Integer MDR_port) throws IOException {
		super(server);
		
		this.MDR_address = InetAddress.getByName(MDR_address);
        this.MDR_port = MDR_port;
        this.MDR = new DatagramSocket();
		
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
	            byte[] buf = new byte[65000];
	
	            DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            MC.receive(packet);
	            String request = new String(buf, 0, buf.length, StandardCharsets.ISO_8859_1);
	            request = request.trim();
	            //Print request & continue IF fileId is in this peer's system
	            if(server.files.get(request.split(" ")[3]) != null) {
	            	System.out.println("Peer "+server.ID+": received request - "+request);
	            }
	            else {
	            	continue;
	            }
	            protocol(request.split(" "));
            }catch(IOException | InterruptedException | NoSuchAlgorithmException e){
            	System.out.println("Exception caught in Server thread");
            }
	    }
	}

private void protocol(String[] request) throws IOException, InterruptedException, NoSuchAlgorithmException {
		
		String operation = request[0];
		
		if (operation.compareTo("STORED") == 0 || operation.compareTo("GETCHUNK") == 0) {
			String version = request[1];
			//String senderId = request[2];
			String fileId = request[3];
			String chunkNo = request[4];
			String flags = request[5];
			
			if(!flags.contains(server.CRLF+server.CRLF)){
	        	System.out.println("	Invalid flags");
	        	return;
	        }
			
			//BACKUP
			if(operation.compareTo("STORED") == 0) {
				Integer replicationDeg = server.files.get(fileId).getReplicationDeg();
	            server.files.get(fileId).setReplicationDeg(replicationDeg+1);
			}
			//RESTORE
			else if(operation.compareTo("GETCHUNK") == 0) {
				Path path = Paths.get("src/Chunks/"+fileId+"/"+chunkNo);
				byte[] chunk = Files.readAllBytes(path);
				
				//Broadcast after random delay
	    		Random rand = new Random();
	    		int delay = rand.nextInt(400);
	    		Thread.sleep(delay);
	    		
	    		String msg = "CHUNK "+version+" "+server.ID+" "+fileId+" "+chunkNo+" "+server.CRLF+server.CRLF+chunk;
	    		
	    		DatagramPacket packet = new DatagramPacket(msg.getBytes(Charset.forName("ISO_8859_1")), msg.length(), MDR_address, MDR_port);
	            MDR.send(packet);
	            
	            //Broadcast end of protocol
	    		System.out.println("Peer "+server.ID+": finished GETCHUNK protocol");
			}
		}

		//DELETE
		else if (operation.compareTo("DELETE") == 0) {
			//String senderId = request[2];
			String fileId = request[3];
			String flags = request[4];
			
			if(!flags.contains(server.CRLF+server.CRLF)){
	        	System.out.println("	Invalid flags");
	        	return;
	        }
			//Delete Chunks
			Path filesPath = Paths.get("src/Chunks/" + fileId);
			String filePath = filesPath.toAbsolutePath().toString();
			
			File chunk = new File(filePath);
			deleteDir(chunk);
			System.out.println("Chunks of FileId: " + fileId + " have been successfuly deleted.");
			
			//Delete File from src/Files
            File backedUpFiles = new File("./src/Files");
            File[] filesList = backedUpFiles.listFiles();
            
            for(File f : filesList){
                if(f.isFile()){
                	Path temp_path = Paths.get("src/Files/" + f.getName());
                	String flpath = temp_path.toAbsolutePath().toString();
                	String tempFileId = Server.getFileId(flpath);
                	
                	if (tempFileId.compareTo(fileId) == 0) {
                		Path pathToDeleteFile = Paths.get("./src/Files/" + f.getName());
                		Files.delete(pathToDeleteFile);
                		System.out.println("File : " + f.getName() + " has been successfuly deleted.");
                		break;
                	}
                }
            }
            
			//Broadcast end of protocol
    		System.out.println("Peer "+server.ID+": finished DELETE protocol");
		}
		else {
			 System.out.println("Incorrect message sent to Control Listener..");
		}
	}
	
	public static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
}
