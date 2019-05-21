
import java.awt.*;
import java.lang.reflect.Field;
import java.net.*;
import java.io.*;
import java.util.ArrayList;


/*
Course: 20203 - Techniques in Programming
Name: Khiem Nguyen
Project: Lab 4&5
Objective: Write a chat server using java.net.Socket and java.net.ServerSocket
How To Use:
    - Client will connect to the port 5555 to enter this Server chat room
    - --help to see other options
*/

public class lab4Server {

    final static int PORT = 5555;   //port number should be > 1023
    static int clientNum = 0;
//ArrayList of client in order to send messages
    static ArrayList<chatServerThread> clientList = new ArrayList<>();

    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + PORT + ", " + e);
            System.exit(1);
        }

        while (true) {
            Socket clientSocket;
            BufferedReader is;
            PrintWriter os;

        //Listen to accept the incoming client socket
            try {
                clientSocket = serverSocket.accept();
                is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                os = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()));
            } catch (IOException e) {
                System.out.println("Accept failed: " + PORT + ", " + e);
                continue;
            }

        //Create new instance of client using its socket and incremented number of clients
            clientNum++;
            chatServerThread newClient = new chatServerThread(clientSocket, "Client_" + clientNum, is, os);
            clientList.add(newClient);
            newClient.start();
        }

    }

    public static class chatServerThread extends Thread {
        private Socket socket;
        private String name;
        private BufferedReader is;
        private PrintWriter os;
        private String signature = "";
        private String helpMes = "***Welcome to Simple Chat Client, here are some basic commands which can help you along the way:\n" +
                        "--clear                      : clear all text above \n" +
                        "--name myName      : change current nickname to myName\n" +
                        "--sign mySign          : change current signature to mySign. mySign will be added at the end of sentence.\n" +
                        "--colorB myColor       : change current background color to myColor (Options: red/RED/blue/BLUE...)\n" +
                        "--colorT myColor       : change current text color to myColor (Options: red/RED/blue/BLUE...)\n" +
                        "--online                      : to see current online client(s)\n" +
                        "--quit                         : to quit the Chat Server\n" +
                        "--help                      : for command menu\n";
        private boolean isFirst = true;

        chatServerThread(Socket socket, String name, BufferedReader is, PrintWriter os) {
            this.socket = socket;
            this.name = name;
            this.is = is;
            this.os = os;
        }

        public void run() {
            try {
                if (isFirst) {
                    this.os.println(helpMes);
                    this.os.flush();
                    isFirst = false;
                }
                while (!this.socket.isClosed()) {
                    String inputLine, outputLine = "";
                    while ((inputLine = this.is.readLine()) != null) {
                        outputLine = inputLine;

                    //Split the outputLine to check for command
                        String[] token = outputLine.split(" ");

                    //Do nothing when receiving empty text
                        if (token.length == 0 || outputLine.equals(""))
                            System.out.println("Received empty text.");

                    //--clear to clear chat board
                        else if (token[0].equals("--clear"))
                            if (token.length != 1)
                                this.os.println("wrongCommand");
                            else
                                this.os.println("--clear");

                    //--name myName: to change client name to myName
                        else if (token[0].equals("--name"))
                            if (token.length != 2)
                                this.os.println("wrongCommand");
                            else {
                                this.name = token[1];
                                this.os.println("Successfully change name to " + this.name);
                            }

                    //--sign mySign: to change signature at the end to mySign
                        else if (token[0].equals("--sign"))
                            if (token.length != 2)
                                this.os.println("wrongCommand");
                            else {
                                this.signature = token[1];
                                this.os.println("Successfully change signature to " + this.signature);
                            }

                    //--colorT myColor or --colorB myColor: to change text or background color to myColor
                        else if (token[0].equals("--colorT") || token[0].equals("--colorB") && token.length == 2) {
                            boolean valid = true;
                            String color = "";
                            try {
                                color = token[1];
                                Field field = Color.class.getField(color);
                                Color testColor = (Color) field.get(null);
                            } catch (Exception e) {
                                this.os.println("wrongCommand");
                                valid = false;
                            }
                            if (valid) {
                                this.os.println("Successfully change text color to " + color.toUpperCase());
                                this.os.println(outputLine);
                            }
                        }
                        
                    //--online to see who are currently online    
                        else if (outputLine.equals("--online")) {
                            String onlineList = "ONLINE CLIENT(s): ";
                            for (int i = 0; i < clientList.size(); i++) {
                                if (!clientList.get(i).socket.isClosed())
                                    onlineList = onlineList + clientList.get(i).name + ", ";
                            }
                            this.os.println(onlineList);
                            this.os.flush();
                        }
                    //--help for command menu
                        else if (outputLine.equals("--help"))
                            this.os.println(helpMes);

                    //--quit or click Disconnect to disconnect the client
                        else if (outputLine.equals("--quit")) {
                            this.os.println(outputLine);
                            this.os.flush();
                            this.socket.close();
                        }

                        else
                            for (int i = 0; i < clientList.size(); i++) {
                                clientList.get(i).os.println(this.name + ": " + outputLine + " " + signature);
                                clientList.get(i).os.flush();
                        }
                        this.os.flush();
                    }
                }
                this.os.close();
                this.is.close();
                this.socket.close();

            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
        }
           
    }
}
