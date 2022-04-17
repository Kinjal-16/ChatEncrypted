import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Client extends Thread {
    private static Socket clientSocket;
    private static DataInputStream in;
    static String ip;
    static int port;
    private int KEY_SIZE = 128;
    private int T_LEN = 128;
    private byte[] IV;
    private static DataOutputStream out;
    private static HashMap<String, SecretKeySpec> map=new  HashMap<String,SecretKeySpec>();
    private static byte [] key;
    static double serverB;
    static int a;
    static int p;
    public Client(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        clientSocket= new Socket(ip, port);
    }

    public void run() {

        while (true) {
            String msg = null;
            try {
                msg = in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(msg.equals("#keys")) // Getting the public key of the new user
            {
                String str="";
                try {
                    str=in.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String strtmp[]=str.split(":");
                double key = ((Math.pow(Double.parseDouble(strtmp[1]), a)) % p);
                map.put(strtmp[0],setKey(Double.toString(key)));

            }
            else if(msg.equals("Sent"))
            {
                System.out.println("Server:" + msg);
            }
            else
            {
                String temp[]=msg.split(":");
                System.out.println(temp[0]+":"+decrypt(temp[1],map.get(temp[0])));

                }


            }

        }






    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
    public static String decrypt(final String strToDecrypt, final SecretKeySpec secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder()
                    .decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
    public static SecretKeySpec setKey(final String myKey) {
        MessageDigest s = null;
        SecretKeySpec secretKey = null;
        try {
            key = myKey.getBytes("UTF-8");
            s = MessageDigest.getInstance("SHA-1");
            key = s.digest(key);
            key = Arrays.copyOf(key, 16);

            secretKey=new SecretKeySpec(key, "AES");

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return secretKey;
    }
    public static void main(String args[]) throws IOException {
        String Astr;
        String serverName = "127.0.0.1";
        int port = 50001;

         p= 100106597;
        int base = 9;
        a = (int)(Math.random()*(8-1+1)+1);

        Client ob=new Client("127.0.0.1", 6666);
        try {
            in = new DataInputStream(clientSocket.getInputStream());

            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }



        double A = ((Math.pow(base, a)) % p); // calculation of A
        Astr = Double.toString(A);

        out.writeUTF(Astr); // Sending A

        String str=in.readUTF();
        // Getting the public keys of existing users
        while(!str.equals("done")) {
            String strtemp[]=str.split(":");
            serverB = Double.parseDouble(strtemp[1]);
            double key = ((Math.pow(serverB, a)) % p);

            map.put(strtemp[0],setKey(Double.toString(key)));
            str=in.readUTF();

        }





        sendMessage msg=new sendMessage(map,out);
        msg.start();
        ob.start();


        }

}


class sendMessage extends Thread
{
    private DataOutputStream out;
    private static HashMap<String, SecretKeySpec> map=new  HashMap<String,SecretKeySpec>();
    sendMessage(HashMap<String, SecretKeySpec> map,DataOutputStream out)
    {
        this.map=map;
        this.out=out;
    }
    public static String encrypt(final String strToEncrypt,SecretKeySpec secretKey) {
        try {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }



    public void run()
    {
        while(true)
        {
            try {
                sendMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void sendMessage() throws IOException {
        Scanner sc = new Scanner(System.in);

        String msg = sc.nextLine();
        for(String str: map.keySet())
        {

            String encryptMsg=encrypt(msg,map.get(str));
            out.writeUTF(str+":"+encryptMsg);
        }

        if(msg.equals("logout"))
        {
            System.exit(0);
        }

    }

}