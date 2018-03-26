import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

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
                try {
                    protocol(request.split(" "));
                }catch(NoSuchAlgorithmException e){
                    System.out.println("NoSuchAlgorithmException caught in Server thread");
                }
            }catch(IOException e){
                System.out.println("IOException caught in Server thread");
            }
        }
    }

    private void protocol(String[] request) throws NoSuchAlgorithmException{
        if(request[1].compareTo("BACKUP") == 0){
            String filePath = request[2], fileId = getFileId(filePath), replicationDeg = request[3];
            File file = new File(filePath);
            Path path = new Path(filePath);
            byte[] chunks = Files.readAllBytes(path);

            //Broadcast protocol to use
            System.out.println("Peer: "+ID+" starting BACKUP protocol");

            //Start sending chunks to the multicast data channel(MDB)
            int i;
            for(i = 0; i < file.length()/64000; i++){
                int chunkNo = i;
                //Prepare HEADER
                String header = "PUTCHUNK " + version + " " + ID + " " + fileId + " " + chunkNo + " " + replicationDeg + " " + CRLF + CRLF;
                //Prepare BODY
                String body = new String(Arrays.copyOfRange(chunks, i*64000, (i+1)*64000), StandardCharsets.UTF_8);
                //Create chunk
                String chunk = header + body;

                DatagramPacket packet = new DatagramPacket(chunk.getBytes();, chunk.length, MDB_address, MDB_port);
                MDB.send();
            }


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

    private String getFileId(String fileName) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte rand[] = new byte[20];
        random.nextBytes(rand);

        String string = fileName + ID + rand;

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
}