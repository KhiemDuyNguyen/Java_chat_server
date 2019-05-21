import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.InterruptedException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/*
Course: 20203 - Techniques in Programming
Name: Khiem Nguyen
Project: Lab 4&5
Objective: Write a chat server using java.net.Socket and java.net.ServerSocket
How To Use:
    - Client will connect to the port 5555 to enter this Server chat room
    - --help to see other options
*/

public class lab5Client extends JFrame implements ActionListener {
    JTextArea result = new JTextArea(25,50);
    JTextField userInput = new JTextField(32);
    JTextField serverInput = new JTextField("localhost",12);
    JTextField portInput = new JTextField("5555",5);
    JPanel panel1 = new JPanel(new FlowLayout());
    JPanel panel2 = new JPanel(new FlowLayout());

    JButton connectButton = new JButton("Connect");
    JButton sendButton = new JButton("Send");
    JLabel errors = new JLabel();
    JLabel serverLb = new JLabel("Server:");
    JLabel portLb = new JLabel("Port:");
    JScrollPane scroller = new JScrollPane();
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    Thread thread;

    public lab5Client() {
        setLayout(new java.awt.FlowLayout());
        setSize(625,550);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        result.setEditable(false);
        scroller.getViewport().add(result);
        setTitle("Simple Chat Client");
        add(scroller);
        panel1.add(userInput); userInput.addActionListener(this);
        panel1.add(sendButton); sendButton.addActionListener(this); sendButton.setEnabled(false);

        panel2.add(serverLb); panel2.add(serverInput); panel2.add(portLb); panel2.add(portInput);
        panel2.add(connectButton); connectButton.addActionListener(this);
        add(panel1); add(panel2);
        add(errors);
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            if (evt.getActionCommand().equals("Connect") ||
                    connectButton.getText().equals("Connect") && evt.getSource() == userInput) {

                socket = new Socket(serverInput.getText(), Integer.parseInt(portInput.getText()));
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), false);
                thread = new ReadThread(in, result);
                thread.start();
                sendButton.setEnabled(true);
                connectButton.setText("Disconnect");
                userInput.setText("");
                result.setText("");
            }
            else if (evt.getActionCommand().equals("Disconnect")) {
                out.print("--quit");
                out.flush();
                thread.interrupt();
                socket.close();
                in.close();
                out.close();
                sendButton.setEnabled(false);
                connectButton.setText("Connect");
            }
            else if (evt.getActionCommand().equals("Send") ||
                    sendButton.isEnabled() && evt.getSource() == userInput) {
                out.print(userInput.getText() + "\r\n");
                out.flush();
                userInput.setText("");
            }
        } catch(UnknownHostException uhe) {
            errors.setText(uhe.getMessage());
        } catch(IOException ioe) {
            errors.setText(ioe.getMessage());
        }
    }

    public static void main(String[] args) {
        lab5Client display = new lab5Client();
        display.setVisible(true);
    }
}

class ReadThread extends Thread {
    BufferedReader in;
    JTextArea display;
    public ReadThread(BufferedReader br, JTextArea jta) {
        in = br;
        display = jta;
    }
    public void run() {
        String s;
        try {
            while ((s = in.readLine()) != null) {
                if (s.equals("--clear"))
                    display.setText("");
                else if (s.equals("wrongCommand"))
                    display.append("Wrong command format. Try --help for menu.\n");
                else if (s.split(" ")[0].equals("--colorB") && s.split(" ").length == 2)
                {
                    Color color;
                    try {
                        Field field = Color.class.getField(s.split(" ")[1].toUpperCase());
                        color = (Color)field.get(null);
                    } catch (Exception e) {
                        color = null; // Not defined
                    }
                    display.setBackground(color);
                }

                else if (s.split(" ")[0].equals("--colorT") && s.split(" ").length == 2)
                {
                    Color color;
                    try {
                        Field field = Color.class.getField(s.split(" ")[1].toUpperCase());
                        color = (Color)field.get(null);
                    } catch (Exception e) {
                        color = null; // Not defined
                    }
                    display.setForeground(color);
                }

                else if (s.equals("--quit")) {
                    display.append("\nQuitting the server...\n");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("Got interrupted!");
                    }

                }

                else
                    display.append(s + '\n');
            }
        } catch (IOException ioe) {
            System.out.println("Error reading from socket");
        }
    }
}
