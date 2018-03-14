import java.io.*;
import java.net.*;
import java.lang.Runnable;
import java.util.concurrent.*;

public class Client {

    private String mcast_addr;
    private InetAddress srvc_address;
    private Integer srvc_port;
    private static DatagramSocket serviceSocket;
    private Integer mcast_port;
    private String oper;
    private String[] opnd;

    public static void main(String[] args) throws IOException {

        //create Client
        String[] arguments = new String[2];
        for(int i = 0; i < 2; i++) {
            if (args.length >= 4 + i)
                arguments[i] = args[3 + i];
        }
        Client client = new Client(args[0], Integer.parseInt(args[1]), args[2], arguments);

        // join Multicast group
        MulticastSocket multiSocket = new MulticastSocket(client.mcast_port);
        multiSocket.setTimeToLive(1);
        InetAddress address = InetAddress.getByName(client.mcast_addr);
        multiSocket.joinGroup(address);
        System.out.println("Joined Multicast server - Addr: " + client.mcast_addr + ", Port: " + client.mcast_port);

        //get Service info
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, client.mcast_port);
        multiSocket.receive(packet);
        String broadcast = new String(packet.getData(), 0, packet.getLength());
        String[] params = broadcast.split(",");
        client.srvc_address = InetAddress.getByName(params[0]);
        client.srvc_port = Integer.parseInt(params[1]);

        // send request
        buf = client.getCommand().getBytes();
        packet = new DatagramPacket(buf, buf.length, client.srvc_address, client.srvc_port);
        serviceSocket.send(packet);
        System.out.println("Sent request");

        // get response
        buf = new byte[256];
        packet = new DatagramPacket(buf, buf.length);
        multiSocket.receive(packet);

        // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Received response: " + received);

        multiSocket.leaveGroup(address);
        multiSocket.close();
        serviceSocket.close();
    }

    private Client(String host, Integer port, String operation, String[] arguments) throws SocketException{
        mcast_addr = host;
        mcast_port = port;
        oper = operation;
        opnd = arguments;
        System.out.print("New Client: "+host+", "+port+", "+operation);
        for (String argument : arguments)
            if (argument != null)
                System.out.print(", " + argument);
        System.out.println();
        serviceSocket = new DatagramSocket();
    }

    private String getCommand(){
        System.out.print("Creating command: ");

        String command;
        command = oper;
        for (String anOpnd : opnd)
            if (anOpnd != null)
                command = command.concat(" " + anOpnd);

        System.out.println(command);
        System.out.println("Command created.");

        return command;
    }
}