package Client;

import SQL.Connect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client extends JFrame {
    // адрес сервера
    private static final String SERVER_HOST = "localhost";
    // порт
    private static final int SERVER_PORT = 3443;
    // клиентский сокет
    private Socket clientSocket;
    // входящее сообщение
    private Scanner inMessage;
    // исходящее сообщение
    private PrintWriter outMessage;
    // следующие поля отвечают за элементы формы
    private JTextField jtfMessage;
    private JTextField jtfName;
    private JTextArea jtaTextAreaMessage;
    // имя клиента
    private String clientName = "";
    private Connect connect = new Connect();


    // получаем имя клиента
    public String getClientName() {
        return this.clientName;
    }

    // конструктор
    public Client() {
        try {
            // подключаемся к серверу
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Задаём настройки элементов на форме
        setBounds(600, 300, 600, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jtaTextAreaMessage = new JTextArea();
        jtaTextAreaMessage.setEditable(false);
        jtaTextAreaMessage.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
        add(jsp, BorderLayout.CENTER);
        //меню
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createHistoryMenu());
        setJMenuBar(menuBar);
        // label, который будет отражать количество клиентов в чате
        JLabel jlNumberOfClients = new JLabel("Людей онлайн: ");
        add(jlNumberOfClients, BorderLayout.NORTH);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        JButton jbSendMessage = new JButton("Отправить");
        bottomPanel.add(jbSendMessage, BorderLayout.EAST);
        jtfMessage = new JTextField("Введите ваше сообщение: ");
        bottomPanel.add(jtfMessage, BorderLayout.CENTER);
        jtfName = new JTextField("Введите ваше имя: ");
        bottomPanel.add(jtfName, BorderLayout.WEST);
        // обработчик события нажатия кнопки отправки сообщения
        jbSendMessage.addActionListener(e -> {
            if (jtfMessage.getText().equals("ipconfig")) {
                try {
                    jtaTextAreaMessage.append("Ваш IP -> " + InetAddress.getLocalHost().getHostAddress());
                } catch (IOException ex) {
                }
                jtaTextAreaMessage.append("\n");
                return;
            }
            if (jtfMessage.getText().contains("@send_file")){
                if (jtfName.getText().trim().isEmpty()) return;
                try {
                    sendFile(findFile(jtfMessage.getText()));
                }catch (Exception e1){}
                jtfMessage.grabFocus();
                return;
            }
            // если имя клиента, и сообщение непустые, то отправляем сообщение
            if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
                clientName = jtfName.getText();
                sendMsg();
                // фокус на текстовое поле с сообщением
                jtfMessage.grabFocus();
            }

        });
        // при фокусе поле сообщения очищается
        jtfMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfMessage.setText("");
            }
        });
        // при фокусе поле имя очищается
        jtfName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfName.setText("");
            }
        });
        // в отдельном потоке начинаем работу с сервером
        new Thread(() -> {
            try {
                // бесконечный цикл
                while (true) {
                    // если есть входящее сообщение
                    if (inMessage.hasNext()) {
                        // считываем его
                        String inMes = inMessage.nextLine();
                        String clientsInChat = "Людей онлайн = ";
                        if (inMes.indexOf(clientsInChat) == 0) {
                            jlNumberOfClients.setText(inMes);
                        } else {
                            // выводим сообщение
                            jtaTextAreaMessage.append(inMes);
                            // добавляем строку перехода
                            jtaTextAreaMessage.append("\n");
                        }
                    }
                }
            } catch (Exception e) {
            }
        }).start();
        // добавляем обработчик события закрытия окна клиентского приложения
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    // здесь проверяем, что имя клиента непустое и не равно значению по умолчанию
                    if (!clientName.isEmpty() && !clientName.equals("Введите ваше имя: ")) {
                        outMessage.println(clientName + " вышел из чата!");
                    } else {
                        outMessage.println("Участник вышел из чата, так и не представившись!");
                    }
                    // отправляем служебное сообщение, которое является признаком того, что клиент вышел из чата
                    outMessage.println("##session##end##");
                    outMessage.flush();
                    outMessage.close();
                    inMessage.close();
                    clientSocket.close();
                    connect.closeConnection();

                } catch (IOException exc) {

                }
            }
        });
        // отображаем форму
        setVisible(true);
    }

    private JMenu createHistoryMenu() {
        // Создание выпадающего меню
        JMenu history = new JMenu("История");
        // Пункт меню "Открыть" с изображением
        JMenuItem open = new JMenuItem("Предоставить историю переписки");
        JMenuItem delete = new JMenuItem("Удалить историю переписки");
        history.add(open);
        history.add(delete);
        open.addActionListener(l->{
            String from = JOptionPane.showInputDialog(this," с какого периода вы хотите полчить историю? HH:MM:SS");
            if (!jtfName.getText().isEmpty())
                jtaTextAreaMessage.append("История переписки пользователя " +
                        jtfName.getText()+ "\n" +connect.getStory(from,jtfName.getText()));
            else
                jtaTextAreaMessage.append("Вы не ввели имя\n");
        });
        delete.addActionListener(l->{
            if (!jtfName.getText().isEmpty())
                connect.detele(jtfName.getText());
            else
                jtaTextAreaMessage.append("Вы не ввели имя\n");
        });
        return history;
    }

    public void sendMsg() {
        // формируем сообщение для отправки на сервер
        DateFormat df = DateFormat.getTimeInstance(DateFormat.DEFAULT);

        String messageStr = jtfName.getText() + " (" + df.format(new Date()) + ") : " + jtfMessage.getText();
        //сохраняем сообщение в базе
        connect.saveMessage(jtfName.getText(),jtfMessage.getText(),df.format(new Date()));
        // отправляем сообщение
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }

    private void sendFile(String filename) throws IOException{
        String messageStr = jtfName.getText()+" sent a file " +
                filename + "  >>  [" + readFile(filename) + "]";
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }

    private String findFile(String s){
        String res = "";
        for (int i = 0; i < s.length();i++){
            if (s.charAt(i) == '['){
                while (s.charAt(i) != ']'){
                    i++;
                    if (s.charAt(i) == ']') break;
                    res += s.charAt(i);
                }
                break;
            }
        }
        return res;
    }

    private String readFile(String filename) throws IOException{
        return new String(Files.readAllBytes(Paths.get(filename)));
    }
}
