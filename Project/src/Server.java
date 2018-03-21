package src;

import java.io.IOException;
import java.security.MessageDigest;
import java.net.*;

public class Server extends Thread{
    //ID & Version
    private String ID;
    private String version = "1.0";
    private String CRLF = Integer.toString(0xD) + Integer.toString(0xA);

    //Server Channel
    private DatagramSocket SC;
    private InetAddress SC_address;
    private Integer SC_port;

    //Control Channel
    private MulticastSocket MC;
    private InetAddress MC_address;
    private Integer MC_port;

    //Backup Channel
    private MulticastSocket MDB;
    private InetAddress MDB_address;
    private Integer MDB_port;

    //Restore Channel
    private MulticastSocket MDR;
    private InetAddress MDR_address;
    private Integer MDR_port;

    public Server(String MC_address, Integer MC_port, String MDB_address, Integer MDB_port, String MDR_address, Integer MDR_port) throws IOException, UnknownHostException, IOException{
        SC_port = 4445;
        SC_address = InetAddress.getLocalHost();
        SC = new DatagramSocket(SC_port);

        ID = SC_address + ":" + SC_port;

        MC = new MulticastSocket(MC_port);
        this.MC_address = InetAddress.getByName(MC_address);
        this.MC_port = MC_port;
        MC.joinGroup(this.MC_address);

        MDB = new MulticastSocket(MDB_port);
        this.MDB_address = InetAddress.getByName(MDB_address);
        this.MDB_port = MDB_port;
        MDB.joinGroup(this.MDB_address);

        MDR = new MulticastSocket(MDR_port);
        this.MDR_address = InetAddress.getByName(MDR_address);
        this.MDR_port = MDR_port;
        MDR.joinGroup(this.MDR_address);
    }

    public static void main(String[] args) throws UnknownHostException, InterruptedException, IOException {
        //Initialize Peer thread
        Server server;
        if(args.length != 0) {
            server = new Server(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), args[4], Integer.parseInt(args[5]));
        }else{
            server = new Server("224.0.0.2", 8001, "224.0.0.3", 8002, "224.0.0.4", 8003);
        }
    	server.start();
    }

    @Override
    public void run() {
        while(true){
            try {
                //Retrieve packet from the Control channel
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                SC.receive(packet);
                String request = new String(buf, 0, buf.length);
                request = request.trim();
                //Print request
                System.out.println("Peer: "+ID+" received request - "+request);
                //Analize request & execute protocol
                protocol(request.split(" "));
            }catch (IOException e){
                System.out.println("IOException caught in Server thread");
            }
        }
    }

    private void protocol(String[] request) {
    	String fileId, chunkNo, replicationDeg;
    	fileId = "";
    	chunkNo = "";
    	
    	//BACKUP
        if(request[0].compareTo("BACKUP") == 0){
            
            //Broadcast protocol to use
            System.out.println("Peer: "+ID+" starting BACKUP protocol");

            //Prepare HEADER
            //String header = "PUTCHUNK " + version + " " + ID + " " + fileId + " " + chunkNo + " " + replicationDeg + " " + CRLF;
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
    
    private static boolean validArgumentNumber(String[] args) {
    	if (args.length != 3) {
    		System.out.println("multicast: <mcast_addr> <mcast_port>: <srvc_addr> <srvc_port> ");
    		return false;
    	}
    	return true;
    }
    
    private static void parseArguments(String[] args) {
    	//INET_ADDR = args[0];
    	//PORT = Integer.parseInt(args[1]);
    }
}