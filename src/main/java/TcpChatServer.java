import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TcpChatServerManager {
    private List<Socket> socketList;

    //생성자 : TCSM 객체를 생성하면 소켓리스트 하나 만듬
    public TcpChatServerManager() {
       socketList = new ArrayList<Socket>();
    }
    //소켓 추가 메소드
    public void addSocket(Socket socket) {
        this.socketList.add(socket);
        new Thread(new ReceiverThread(socket)).start();
    }

    //멀티 클라이언트와 연결을 동시에 유지하기 위한 스레드 구성
    //각각의 소켓 정보를 가지고 있어야함
    class ReceiverThread implements Runnable {
        private Socket socket;

        public ReceiverThread(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while(true){
                    String msg = br.readLine(); //클라이언트가 보낸 메시지 읽기
                    System.out.println(msg);
                    //받은 메시지를 메시지 보낸 클라이언트 제외하고 모든 클라이언트에게 보내기
                    Socket tmpSocket = null;
                    try{
                        for (int i = 0; i < socketList.size(); i++) {
                            tmpSocket = socketList.get(i);
                            //socketList.get(0) -> 소켓 객체
                            if(socket.equals(tmpSocket)) continue;
                            //메시지를 보낸 클라이언트라면 반복을 한번 건너뛰기

                            //서버가 받은 메시지를 클라이언트에 송신하기
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(tmpSocket.getOutputStream()));
                            bufferedWriter.write(msg+'\n');
                            bufferedWriter.flush();
                        }
                    }catch (Exception e) {
                        System.out.println(tmpSocket.getRemoteSocketAddress()+"연결종료");
                        socketList.remove(tmpSocket);
                        //연결을 끝낸 클라이언트를 위한 소켓 제거

                        System.out.println("===================현재 참여자===================");
                        for(Socket s : socketList) {
                            System.out.println(s.getRemoteSocketAddress());
                        }
                        System.out.println("================================================");
                    }
                }
            }catch (IOException e) { }
            finally {
                if(socket != null) {
                    System.out.println(socket.getRemoteSocketAddress()+"연결종료");
                    socketList.remove(socket);
                    //연결을 끝낸 클라이언트를 위한 소켓 제거
                    System.out.println("===================현재 참여자===================");
                    for(Socket s : socketList) {
                        System.out.println(s.getRemoteSocketAddress());
                    }
                    System.out.println("================================================");
                }
                }
            }

        }
    }
    public class TcpChatServer {
        private static ServerSocket serverSocket;

        public static void main(String[] args) {
            //서버 소켓
            TcpChatServerManager tcpChatServerManager = new TcpChatServerManager();
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress("localhost", 5001));
                while (true) {
                    Socket socket = serverSocket.accept(); //클라이언트 연결 요청 대기 (연결 요청 오기 전엔 블로킹)
                    /*
                    연결이 요청이 오면? 소켓을 반환
                    멀티 클라이언트 => 소켓이 여러개 => 리스트로 관리
                    =>서버 메니저 클래스로 관리
                     */
                    System.out.println(socket.getRemoteSocketAddress()+ " : 연결");
                    tcpChatServerManager.addSocket(socket); //얻은 소켓 서버 매니저의 소켓 목록에 추가
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

}

