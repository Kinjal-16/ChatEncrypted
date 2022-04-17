// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.*;
import java.util.*;
import java.net.*;

// Server class
public class Server
{

    // Vector to store active clients
    static HashSet<ClientHandler> set = new HashSet<ClientHandler>();
    static HashMap<String,Double> map=new HashMap<String,Double>();
    // counter for clients
    static int i = 0;

    public static void main(String[] args) throws IOException
    {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(6666);

        Socket s;

        // running infinite loop for getting
        // client request
        while (true)
        {
            // Accept the incoming request
            s = ss.accept();

            System.out.println("Request for new client received : " + s);

            // obtain input and output streams
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            System.out.println("Creating a new handler for this client...");
            double  clientA=0.0;
            try {
                clientA = Double.parseDouble(in.readUTF()); // to accept A

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            for(String str: map.keySet())
            {
                String d=str+":"+map.get(str);
                out.writeUTF(d);
            }
            out.writeUTF("done");
            for(ClientHandler mc:set)
            {
                mc.out.writeUTF("#keys");
                String msg="client"+i+":"+clientA;
                mc.out.writeUTF(msg);
            }
            map.put("client"+i,clientA);
            System.out.println(map);
            // Create a new handler object for handling this request.
            ClientHandler client = new ClientHandler(s,"client"+i, in, out);

            // Create a new Thread with this object.
            Thread t = new Thread(client);

            System.out.println("Adding this client to the group");

            // add this client to active clients list
            if(set.add(client)==true) {
                set.add(client);
                t.start();
            }
            else {
                System.out.println("Already have a connection with the client");
                for(ClientHandler find:set)
                {
                    if(find.equals(client))
                    {
                        find.isloggedin=true;
                    }
                }
            }


            // start the thread.


            // increment i for new client.
            // i is used for naming only, and can be replaced
            // by any naming scheme
            i++;

        }
    }
}

// ClientHandler class
class ClientHandler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    private String name;
    final DataInputStream in;
    final DataOutputStream out;
    Socket s;
    boolean isloggedin;

    // constructor
    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
        in = dis;
        out = dos;
        this.name = name;
        this.s = s;
        this.isloggedin=true;


    }

    @Override
    public void run() {

        String received;


        // Client p, g, and key

        String str;

        // Established the Connection



        while (true)
        {
            try
            {
                // receive the string
                received = in.readUTF();

                System.out.println(received);

                if(received.equals("logout")){
                    System.out.println("Logged out");
                    this.isloggedin=false;

                    break;
                }

                // break the string into message and recipient part
                System.out.println("Received");

                String temp[]=received.split(":");


                for (ClientHandler mc : Server.set)
                {
                    // if the recipient is found, write on its
                    // output stream

                    if(mc.name==this.name)
                        mc.out.writeUTF("Sent");
                    else{
                        String tmp=mc.name.trim();

                        if(tmp.equals(temp[0]))
                            mc.out.writeUTF(this.name +":" + temp[1]);
                    }


                }
            } catch (IOException e) {

                e.printStackTrace();
            }

        }
        try
        {
            // closing resources
            this.in.close();
            this.out.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

