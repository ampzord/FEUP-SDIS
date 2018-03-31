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
    	if(request.length <= 1) {
    		System.out.println("Peer "+ID+": Invalid request received");
    		return;
    	}
    		
    	int chunkNo = 0;
    	String filePath = "src/"+request[1], fileId = getFileId(filePath), replicationDeg = request[2];
        File file = new File(filePath);
        Path path = Paths.get(filePath);
        byte[] chunks = Files.readAllBytes(path);
        double Nchunks = file.length()/64000.0;
        if(Nchunks-(int)Nchunks > 0)
        	Nchunks++;
    	
        //BACKUP
        if(request[0].compareTo("BACKUP") == 0){
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
        /*
        //DELETE
        else if (request[0].compareTo("DELETE") == 0) {
        	
        	//Broadcast protocol to use
            System.out.println("Peer: " + ID + " starting DELETE protocol");
        
        	//DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
            String header = "DELETE " + version + " " + ID + " " + fileId + " " + CRLF + CRLF;
        }
        
        //RECLAIM
        else if (request[0].compareTo("RECLAIM") == 0) {
        	
        	//Broadcast protocol to use
            System.out.println("Peer: " + ID + " starting RECLAIM protocol");
        
            //REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
            String header = "REMOVED " + version + " " + ID + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
        }*/
        else {
        	System.out.println("Not valid operation..");
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
}