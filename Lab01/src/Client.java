import java.io.*;
import java.net.*;

public class Client {

    private String host_name;
    private Integer port_number;
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

        // get a datagram socket
        DatagramSocket socket = new DatagramSocket();

        // send request
        byte[] buf = client.getCommand().getBytes();
        InetAddress address = InetAddress.getByName(client.host_name);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, client.port_number);
        socket.send(packet);

        // get response
        buf = new byte[256];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Response: " + received);

        socket.close();
    }

    private Client(String host, Integer port, String operation, String[] arguments){
        host_name = host;
        port_number = port;
        oper = operation;
        opnd = arguments;
        System.out.print("New Client: "+host+", "+port+", "+operation);
        for (String argument : arguments)
            if (argument != null)
                System.out.print(", " + argument);
        System.out.println();
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