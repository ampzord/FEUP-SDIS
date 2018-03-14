import java.net.*;
import java.io.IOException;

public class ServerBroadcast extends Thread {

    private InetAddress address;
    private Integer port;
    private Integer servicePort;
    private DatagramSocket socket;

    public ServerBroadcast(InetAddress Address, Integer Port, Integer ServicePort) throws UnknownHostException, SocketException{
        address = Address;
        port = Port;
        servicePort = ServicePort;
        socket = new DatagramSocket();
    }

    @Override
    public void run() {
        while(true){
            try {
                System.out.println("Server: Service available at IP Address - " + InetAddress.getByName("localhost") + " Port - " + servicePort);
                String broadcast = InetAddress.getByName("localhost") + "," + servicePort;
                byte[] buf = broadcast.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);
                try {
                    Thread.sleep(1000);
                }catch(InterruptedException ignored){
                }
            }catch(IOException ignored){
            }
        }
    }
}