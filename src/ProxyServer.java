import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer
{

    public static void main(String[] args) throws IOException
    {
        int    proxyPort = Integer.parseInt(args[0]);   // Proxy Server Port
        String destIp    = args[1];                     // 목적지 IP
        int    destPort  = Integer.parseInt(args[2]);   // 목적지 Port
        
        System.out.println("##### Proxy Server Daemon Start !! #####");
        System.out.println("Proxy Server Port : " + proxyPort);
        System.out.println("Destination IP    : " + destIp);
        System.out.println("Destination Port  : " + destPort);
        System.out.println("########################################");
        
        runProxyServer(proxyPort, destIp, destPort);
    }

    /*
     * Proxy Server Run
     * 
     * client에서 받고 목적지 server에 보내는 것 외에 목적지 서버에서의 response도 client로 보내야하기 때문에
     * client에서 request 데이터를 받아 목적지 server에 보내는 것은 쓰레드를 구성하여 멀티 쓰레드로 구성
     * 
     * 1. client request -> proxy -> destination server => Thread 1 (new  Thread) sendRequestToDestination
     * 2. destinationserver response -> proxy -> client => Thread 2 (main Thread) sendResponseToClient
     */
    private static void runProxyServer(int proxyPort, String destIp, int destPort) throws IOException {
        System.out.println("##### runProxyServer #####");
        ServerSocket proxyServerSocket = new ServerSocket(proxyPort);
        
        // 요청 계속 받기위해 무한루프
        while(true) {
            Socket clientSocket      = null;                             // client와 연결하는 소켓 생성
            Socket destinationSocket = null;                             // 목적지 서버와 연결하는 소켓 생성
            
            clientSocket      = proxyServerSocket.accept();              // client로부터 요청 대기하기 위한 소켓 init
            destinationSocket = connectDestination(destIp, destPort);    // 목적지 Server와 연결하기 위한 소켓 init

            // client와 데이터 송수신을 위한 I/O Stream 생성
            final InputStream  inClient  = clientSocket.getInputStream();
            final OutputStream outClient = clientSocket.getOutputStream();
            
            // 목적지 Server와 데이터 송수신을 위한 I/O Stream 생성
            final InputStream  inDestination  = destinationSocket.getInputStream();
            final OutputStream outDestination = destinationSocket.getOutputStream();
            
            sendRequestToDestination(inClient, outDestination);          // client request data를 읽어 목적지 server에 보내기
            sendResponseToClient(inDestination, outClient);              // 목적지 server response를 client에 보내기
            
            // client, destination 연결했던 소켓 종료
            if(clientSocket != null)      clientSocket.close();
            if(destinationSocket != null) destinationSocket.close();
        }
    }
    
    private static Socket connectDestination(String destIp, int destPort) {
        System.out.println("##### connectDestination #####");
        Socket sock = null;
        
        try {
            sock = new Socket(destIp, destPort);
        }
        catch(Exception e) {
            System.out.println("##### Failed to connect to Destination Server !! #####");
            System.out.println("Destination IP    : " + destIp);
            System.out.println("Destination Port  : " + destPort);
            System.out.println("######################################################");
        }
        
        return sock;
    }
    
    private static void sendRequestToDestination(InputStream  inClient, OutputStream outDestination) {
        System.out.println("##### sendRequestToDestination #####");
        final byte[] request = new byte[4096];
        
        Thread thread = new Thread() {
            public void run() {
                int readByte;
                
                try {
                    while(-1 != (readByte = inClient.read(request))) {
                        outDestination.write(request, 0, readByte);
                        outDestination.flush();
                    }
                }
                catch(IOException e) {
                }
                finally {
                    try {
                        outDestination.close();
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        
        thread.start();
    }
    
    private static void sendResponseToClient(InputStream  inDestination, OutputStream outClient) {
        System.out.println("##### sendResponseToClient #####");
        byte[] response = new byte[4096];
        int readByte;

        try {
            while(-1 != (readByte = inDestination.read(response))) {
                outClient.write(response, 0, readByte);
                outClient.flush();
            }
        }
        catch(IOException e) {
        }
        finally {
            try {
                outClient.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
