package src;

import java.net.*;
import java.io.IOException;


public class TestApp {

    public static void main(String[] args) throws SocketException, UnknownHostException, IOException, InterruptedException{
        //Start client
        String[] peer_ap = new String[2];
        String request;

        if(args.length > 0) {
            peer_ap = args[0].split(":");
            request = args[1];
            for(int i = 2; i < args.length; i++){
                request += " " + args[i];
            }
        }
        else{
            peer_ap[0] = InetAddress.getLocalHost().getHostName();
            peer_ap[1] = "4445";
            request = "BACKUP TestFile.txt 1";
        }

        int port = Integer.parseInt(peer_ap[1]);
        InetAddress address = InetAddress.getByName(peer_ap[0]);
        DatagramSocket socket = new DatagramSocket();

        //Send request
        byte[] buf = request.getBytes();
        System.out.println("Client: Request - " + new String(buf));
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);

        Thread.sleep(1000);
        
        socket.close();
    }
}