package src;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Hashtable;

public class Server extends Thread{
    //ID & Version
    protected String ID;
    protected String version = "1.0";
    protected String CRLF = Integer.toString(0xD) + Integer.toString(0xA);

    //Server Channel
    protected DatagramSocket SC;
    protected InetAddress SC_address;
    protected Integer SC_port;

    //Backup Channel Listener
    protected BackupListener BL;
    
    //Backup Channel Info
    protected DatagramSocket MDB;
    protected InetAddress MDB_address;
    protected Integer MDB_port;

    //Restore Channel Listener
    protected RestoreListener RL;
    
    //Restore Channel Info
    protected DatagramSocket MDR;
    protected InetAddress MDR_address;
    protected Integer MDR_port;

    //Control Channel Listener
    protected ControlListener CL;
    
    //Control Channel Info
    protected DatagramSocket MC;
    protected InetAddress MC_address;
    protected Integer MC_port;
    
    //Files Backed Up
    protected Hashtable<String,FileInfo> files = new Hashtable<String,FileInfo>();
    
    public static void main(String[] args) throws SocketException, UnknownHostException, IOException{
        //Start peers
        Server peer1 = new Server(4445, "224.0.0.2", 8001, "224.0.0.3", 8002, "224.0.0.4", 8003);
    	peer1.start();
    	
    	Server peer2 = new Server(4455, "224.0.0.2", 8001, "224.0.0.3", 8002, "224.0.0.4", 8003);
    	peer2.start();
    }
    
    public Server(Integer SC_port, String MC_address, Integer MC_port, String MDB_address, Integer MDB_port, String MDR_address, Integer MDR_port) throws IOException, UnknownHostException, IOException{
        SC_address = InetAddress.getLocalHost();
        SC = new DatagramSocket(SC_port);

        ID = SC_address + ":" + SC_port;
        
        BL = new BackupListener(this, MC_address, MC_port, MDB_address, MDB_port);
        BL.start();
        
        this.MDB_address = InetAddress.getByName(MDB_address);
        this.MDB_port = MDB_port;
        this.MDB = new DatagramSocket();
        
        RL = new RestoreListener(this, MC_address, MC_port, MDR_address, MDR_port);
        RL.start();
        
        this.MDR_address = InetAddress.getByName(MDR_address);
        this.MDR_port = MDR_port;
        this.MDR = new DatagramSocket();
        
        CL = new ControlListener(this, MC_address, MC_port, MDR_address, MDR_port);
        CL.start();
        
        this.MC_address = InetAddress.getByName(MC_address);
        this.MC_port = MC_port;
        this.MC = new DatagramSocket();
    }

    @Override
    public void run() {
    	System.out.println("Peer "+ID+": Ready for requests");
    	
        while(true){
            try {
                //Retrieve packet from the Control channel
                byte[] buf = new byte[256];

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                SC.receive(packet);
                String request = new String(buf, 0, buf.length);
                request = request.trim();
                //Print request
                System.out.println("Peer "+ID+": received request - "+request);
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
		
    	if (request[0].compareTo("STATE") == 0) {
		        	
		        	//Broadcast protocol to use
		            System.out.println("Peer: " + ID + " starting STATE protocol");
		            
		            
		            /*	For each file whose backup it has initiated:
				            The file pathname
				            The backup service id of the file
				            The desired replication degree
				            For each chunk of the file:
				            Its id
				            Its perceived replication degree
		            For each chunk it stores:
						Its id
						Its size (in KBytes)
						Its perceived replication degree
						The peer's storage capacity, i.e. the maximum amount of disk space that can be used to store chunks, 
						and the amount of storage (both in KBytes) used to backup the chunks.
					*/
		            
		            String currentDir = "./src/Chunks";
		            
		            //get all folders created in the directory Chunks
		            File directory = new File(currentDir);
		            File[] fList = directory.listFiles();
		            for (File file : fList){
		                if (file.isDirectory()){
		                    System.out.println(file.getName());
		                }
		            }
		            
		            
		            
		}
    	
   
    		int chunkNo = 0;
        	String filePath = "src/"+request[1], fileId = getFileId(filePath), replicationDeg;
            File file = new File(filePath);
            Path path = Paths.get(filePath);
            byte[] chunks = Files.readAllBytes(path);
            double Nchunks = file.length()/64000.0;
            if(Nchunks-(int)Nchunks > 0)
            	Nchunks++;
        	
            //BACKUP
            if(request[0].compareTo("BACKUP") == 0){
            	replicationDeg = request[2];
            	
                //Broadcast protocol to use
                System.out.println("Peer "+ID+": starting BACKUP protocol");
                
                //Add file to the list of backed up files
                Files.createDirectory(Paths.get("src/Chunks/"+fileId));
                files.put(fileId, new FileInfo(filePath, (int)Nchunks, chunkNo));
                //Start sending chunks to the multicast data channel(MDB)
                int i;
                for(i = 0; i < (int)Nchunks; i++){
                    chunkNo = i;

                    //Prepare HEADER
                    String header = "PUTCHUNK " + version + " " + ID + " " + fileId + " " + chunkNo + " " + replicationDeg + " " + CRLF + CRLF;
                    //Prepare BODY
                    String body = new String(Arrays.copyOfRange(chunks, i*64000, (i+1)*64000), StandardCharsets.UTF_8);
                    //Create chunk
                    String chunk = header + body;

                    for(int attempt = 1; attempt <= 5; attempt++) {
    	                DatagramPacket packet = new DatagramPacket(chunk.getBytes(), chunk.length(), MDB_address, MDB_port);
    	                MDB.send(packet);
    	                
    	                Thread.sleep(1000);
    	                if(files.get(fileId).getReplicationDeg() >= Integer.parseInt(replicationDeg))
    	                	break;
                    }
                }
            }
            //RESTORE
            else if (request[0].compareTo("RESTORE") == 0) {
            	//Broadcast protocol to use
                System.out.println("Peer: " + ID + " starting RESTORE protocol");
                
                for(int i = 0; i < files.get(fileId).getNchunks(); i++) {
                	// Header for initiator peer
                    String header = "GETCHUNK " + version + " " + ID + " " + fileId + " " + i + " " + CRLF + CRLF;
                    
    	            for(int attempt = 1; attempt <= 5; attempt++) {
    	                DatagramPacket packet = new DatagramPacket(header.getBytes(), header.length(), MC_address, MC_port);
    	                MC.send(packet);
    	                
    	                Thread.sleep(1000);
    	                
    	                if(RL.chunks.get(chunkNo) != null) {
    	                	Files.write(path, RL.chunks.get(chunkNo));
    	                	break;
    	                }
    	            }    
                }
            }
            
            //DELETE
            else if (request[0].compareTo("DELETE") == 0) {
            	
            	//Broadcast protocol to use
                System.out.println("Peer: " + ID + " starting DELETE protocol");
                
                for(int i = 0; i < files.get(fileId).getNchunks(); i++) {
                	// Header for initiator peer
                	String header = "DELETE " + version + " " + ID + " " + fileId + " " + CRLF + CRLF;
                    
    	            for(int attempt = 1; attempt <= 5; attempt++) {
    	                DatagramPacket packet = new DatagramPacket(header.getBytes(), header.length(), MC_address, MC_port);
    	                MC.send(packet);
    	                
    	                Thread.sleep(1000);
    	                
    	                if(RL.chunks.get(chunkNo) != null) {
    	                	Files.write(path, RL.chunks.get(chunkNo));
    	                	break;
    	                }
    	            }    
                }
            }
            
            //RECLAIM
            else if (request[0].compareTo("RECLAIM") == 0) {
            	
            	//Broadcast protocol to use
                System.out.println("Peer: " + ID + " starting RECLAIM protocol");
            
                String header = "REMOVED " + version + " " + ID + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
                
                DatagramPacket packet = new DatagramPacket(header.getBytes(), header.length(), MC_address, MC_port);
                MC.send(packet);
            }
            
            else {
            	System.out.println("Not valid operation..");
            	return;
            }
    	
        
        
    }
    
    protected String getFileId(String fileName) throws NoSuchAlgorithmException, IOException {
        Path path = Paths.get(fileName);
        
        FileTime creationTime = (FileTime)Files.getAttribute(path, "creationTime");
        String string = fileName + creationTime.toString();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(
                string.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(encodedhash);
    }

    //Source: http://www.baeldung.com/sha-256-hashing-java
    private static String bytesToHex(byte[] hash){
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    public void closeAllSockets(){
    	SC.close();
    	BL.MDB.close();
    	BL.MC.close();
    	RL.MDR.close();
    }
    
    private boolean isProtocol(String[] request) {
    	if (request[0] == "BACKUP" || request[0] == "RESTORE" || request[0] == "DELETE" || request[0] == "RECLAIM")
    		return true;
    	return false;
    }
    
    public static void displayDirectoryContents(File dir) {
        try { 
           File[] files = dir.listFiles();
           for (File file : files) {
              if (file.isDirectory()) {
                 System.out.println("directory:" + file.getCanonicalPath());
                 displayDirectoryContents(file);
              }
           } 
        } catch (IOException e) {
           e.printStackTrace();
        } 
     } 
}