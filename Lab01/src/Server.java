import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class Server extends Thread {

    private DatagramSocket socket;
    private String[] plates = new String[256];
    private String[] owners = new String[256];

    public static void main(String[] args) throws IOException {
        Server server = new Server(Integer.parseInt(args[0]));
        server.start();
    }

    private Server(int port_number) throws IOException {
        socket = new DatagramSocket(port_number);
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
                System.out.println("Server: Request - " + request);
                String[] params = request.split(" ");

                // analyze request
                String response;

                if (params[0].compareTo("REGISTER") == 0) {
                    String plate_number = params[1];
                    String owner_name = params[2];
                    response = this.register(plate_number, owner_name).toString();
                } else if (params[0].compareTo("LOOKUP") == 0) {
                    String plate_number = params[1];
                    response = this.lookup(plate_number);
                } else{
                    System.out.println("Server: Invalid operation.");
                    return;
                }

                // send the response to the client at "address" and "port"
                buf = response.getBytes();
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);
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