package com.networking;
import java.io.*;
import java.net.*;
import java.util.Scanner;

class TCPKRClient {

    /* Following data members contain the client attributes including:
            client node number
            num dvr entries
            dvr entries (line by line)
   */

    static int nodeNum;
    static int numDvrEntries;
    static int [][] dvrEntries;

    public static void main(String argv[]) throws Exception
    {

        readStartUpFile();
        printClientConfig();


        Socket clientSocket = new Socket("localhost", 12121);

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

       /* BufferedReader inFromServer =
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));*/


        // getting the node number to send it to the server
        System.out.println("Sending data: " + genClientData());
        outToServer.writeBytes(genClientData());
        clientSocket.close();

    }

    public static void readStartUpFile() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter a filename for client startup");
        String fileName = sc.nextLine();
        System.out.println("Startup file : " + fileName);
        // code reads the startup file line by line

        int index = 0;
        String line = "";
        int dvrIndex = 0;
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(reader);
            // read the file line by line
            while ((line = bufferedReader.readLine()) != null) {
                if(index==1){
                    nodeNum = Integer.parseInt(line);
                }else if (index==2){
                    numDvrEntries = Integer.parseInt(line);
                    dvrEntries = new int[numDvrEntries][2];
                }else if(index > 2 && index <= (2+numDvrEntries)){
                    // split the string
                    dvrEntries[dvrIndex] = new int[2];
                    String [] strDrv = line.split(" ");
                    for(int i = 0; i<strDrv.length; i++){
                        // init dvr entry
                        dvrEntries[dvrIndex][i]=Integer.parseInt(strDrv[i]);
                    }
                    dvrIndex++;
                }

                index++;
            }
            // now initialize everything to the correct variables

        } catch (FileNotFoundException e) {
            System.out.println("File not found - invalid configuration file");
        } catch (IOException e) {
            System.out.println("Error reading file..");
        }

    }

    public static void printClientConfig(){

        // this prints the current server configuration
        System.out.println("Client Node number : " + nodeNum);
        System.out.println("Num DVR entries : " + numDvrEntries);
        // for each entry
        for(int i =0; i<dvrEntries.length; i++){
            // now loop again
            for(int j = 0; j<dvrEntries[i].length; j++){
                System.out.print(dvrEntries[i][j] + " ");
            }
            System.out.println("");
        }

    }
    
    public static String genClientData() {
    	String clientData = "";
    	clientData+=nodeNum+"\n"+numDvrEntries+"\n";
    	
    	// loop for the number of dvr entries
    	 for(int i =0; i<dvrEntries.length; i++){
    		 String tempDvr = "";
             // now loop again
             for(int j = 0; j<dvrEntries[i].length; j++){
                 //System.out.print(dvrEntries[i][j] + " ");
                 tempDvr+=dvrEntries[i][j]+" ";
             }
            // System.out.println("");
             tempDvr+="\n";
             clientData+=tempDvr;
         }
     return clientData;
    }
    
}