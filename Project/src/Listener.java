package src;

import java.io.IOException;

public class Listener extends Thread{

	//Server
	protected Server server;
	
	public Listener(Server server) throws IOException {
		this.server = server;
	}
	
}
	/*@Override
    public void run() {
        while(true) {
            try {
                //Retrieve packet from the MDB channel
	            byte[] buf = new byte[256];
	
	            DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            MDB.receive(packet);
	            String request = new String(buf, 0, buf.length);
	            request = request.trim();
	            //Print request
	            System.out.println("Peer: "+ID+" received request - "+request);
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
        
        //Check if the sender isn't the same peer
        if(request[0].compareTo("PUTCHUNK") == 0 && request[2] != ID){

            //Broadcast protocol to use
            System.out.println("Peer: "+ID+" starting PUTCHUNK protocol");
            
    		Path file = Paths.get("Chunks/"+fileId);
    		Files.write(file, body.getBytes());
    		
    		//Broadcast after random delay
    		Random rand = new Random();
    		int delay = rand.nextInt(400);
    		Thread.sleep(delay);
    		
    		//Broadcast end of protocol
    		System.out.println("Peer: "+ID+" finished PUTCHUNK protocol");
    		
    		String msg = "STORED "+version+" "+ID+" "+fileId+" "+chunkNo+" "+CRLF+CRLF;
    		
    		DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), MC_address, MC_port);
            MC.send(packet);
        }
    	
        //RESTORE
        else if (request[0].compareTo("RESTORE") == 0) {
        	
        	//Broadcast protocol to use
            System.out.println("Peer: " + ID + " starting RESTORE protocol");
            
            // Header for initiator peer
            // GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
            String header = "GETCHUNK " + version + " " + ID + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
            
            //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
            String response = "CHUNK " + version + " " + ID + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
            
            String body;
        }
        
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
            System.out.println("Peer: " + ID + " starting DELETE protocol");
        
            //REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
            String header = "REMOVED " + version + " " + ID + " " + fileId + " " + chunkNo + " " + CRLF + CRLF;
        }
    }
}*/
