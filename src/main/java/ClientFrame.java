import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;


public class ClientFrame extends JFrame {

    private JTextArea textArea;
    private JTextField sendMsgTextField;
    private JScrollPane scrollPane;
    private Socket socket;
    private BufferedWriter bufferedWriter;

    public ClientFrame() {
        textArea = new JTextArea();
        sendMsgTextField = new JTextField();
        textArea.setEditable(false); //쓰기를 금지함 edit불가
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //As needed 즉 필요에 의해서 내용이 많아지면 스크롤 바가 생긴다.
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //가로 스크롤은 안만든다
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setTitle("chatting");

        sendMsgTextField.addKeyListener(new MsgSendListener());
        add(scrollPane, BorderLayout.CENTER); //프레임에 붙이기
        add(sendMsgTextField, BorderLayout.SOUTH);  //프레임에 붙이기
    }

    //소켓 설정을 위한 세터
    //이제 프레임도 소켓의 정보를 가지게 되었다
    public void setSocket(Socket socket) {
        this.socket = socket;
        try {
            OutputStream outputStream = socket.getOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MsgSendListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) { //키가 눌렀다가 떼어졌을때
            //엔터키가 눌렀다가 뗴어지면 텍스트 필드에 있는 내용이 텍스트 에어리어에 나타나게
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                //각각의 키들이 가지고 있는 코드 값이 나타난다
                //VK_Enter = 상수, 엔터키에 대한 키 값을 의미한다
                String msg = sendMsgTextField.getText();
                System.out.println(msg);
                textArea.append("[나]:" + msg + "\n");
                sendMsgTextField.setText("");
                try {
                    bufferedWriter.write(msg + '\n');
                    bufferedWriter.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } //한문장이 끝났다는 것을 알리기 위해서 bw에 '\n' 붙임
            }

        }
    }

    //내부 클래스로 수신 스레드 작성성
    class TcpClientReceiveThread implements Runnable {
        private Socket socket;

        public TcpClientReceiveThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            //서버로부터 오는 메세지를 읽어서
            //텍스트 에어리어에 추가하기
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    String msg = bufferedReader.readLine();
                    textArea.append("[상대방]: " + msg + '\n');
                }
            } catch (IOException e) {
                textArea.append("연결이 종료되었습니다");
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (Exception e2) {

                }
            }

        }
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", 5001));
            //소켓 객체 생성
            ClientFrame clientFrame = new ClientFrame();
            clientFrame.setSocket(socket); //메인에서 프레임 생성
            TcpClientReceiveThread th1 = clientFrame.new TcpClientReceiveThread(socket);
//       TcpClientReceiveThread가 내부 클래스로 선언되어 있기 때문에 clientFrame으로 접근해서 socket전달
            new Thread(th1).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}