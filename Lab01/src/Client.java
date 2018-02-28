import java.io.*;
import java.net.*;

public class Client {

    public String host_name;
    public Integer port_number;
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
        byte[] buf = new byte[256];
        buf = client.getCommand().getBytes();
        InetAddress address = InetAddress.getByName(client.host_name);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, client.port_number);
        socket.send(packet);

        // get response
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Response: " + received);

        socket.close();
    }

    public Client(String host, Integer port, String operation, String[] arguments){
        host_name = host;
        port_number = port;
        oper = operation;
        opnd = arguments;
        System.out.print("New Client: "+host+", "+port+", "+operation);
        for(int i = 0; i < arguments.length; i++)
            if(arguments[i] != null)
                System.out.print(", " + arguments[i]);
        System.out.println();
    }

    public String getCommand(){
        System.out.print("Creating command: ");

        String command;
        command = oper;
        for(int i = 0; i < opnd.length; i++)
            if(opnd[i] != null)
                command = command.concat(" " + opnd[i]);

        System.out.println(command);
        System.out.println("Command created.");

        return command;
    }
}