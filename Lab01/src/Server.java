import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Server extends Thread {

    private MulticastSocket socket;
    private InetAddress address;
    private Integer mcast_port;
    private String[] plates = new String[256];
    private String[] owners = new String[256];

    public static void main(String[] args) throws IOException {
        Server server = new Server(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
        server.start();
    }

    private Server(int service_port, String multicast_address, int multicast_port) throws IOException {
        Integer srvc_port = service_port;
        address = InetAddress.getByName(multicast_address);
        mcast_port = multicast_port;
        socket = new MulticastSocket(mcast_port);
        socket.joinGroup(address);
        System.out.println("Started server: Addr - "+multicast_address+", Port - "+mcast_port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String request = new String(packet.getData());
                request = request.trim();

                //print request
                System.out.println("Received request: " + request);
                String[] params = request.split(" ");

                // analyze request
                System.out.println("Analyzing request...");
                String response;

                if (params[0].compareTo("REGISTER") == 0) {
                    String plate_number = params[1];
                    String owner_name = params[2];
                    response = this.register(plate_number, owner_name).toString();
                } else if (params[0].compareTo("LOOKUP") == 0) {
                    String plate_number = params[1];
                    response = this.lookup(plate_number);
                } else{
                    System.out.println("Invalid operation.");
                    continue;
                }

                // send the response to the client at "address" and "port"
                buf = response.getBytes();
                packet = new DatagramPacket(buf, buf.length, address, mcast_port);
                socket.send(packet);
                System.out.println("Response sent.");
            } catch (IOException ignored) {
            }
        }
    }

    private Integer register(String plate, String owner){
        int i;
        for(i = 0; i < plates.length; i++){
            if(plates[i] == null)
                break;
            if(plates[i].compareTo(plate) == 0){
                System.out.println("Server: Plate already registered.");
                return -1;
            }
        }
        plates[i] = plate;
        owners[i] = owner;

        System.out.println("Server: New plate registered.");
        return i+1;
    }

    private String lookup(String plate){
        int i;
        for(i = 0; i < plates.length; i++){
            if(plates[i] == null)
                break;
            if(plates[i].compareTo(plate) == 0){
                System.out.println("Server: Plate registered to " + owners[i]);
                return owners[i];
            }
        }
        System.out.println("Server: Plate isn't registered.");
        return "NOT_FOUND";
    }
}