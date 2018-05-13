package com.networking;

import javax.swing.text.html.HTMLDocument;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Stream;


/**
 * Created by kyleharris on 5/11/18.
 */

class TCPKRServer {

    /* Following data members contain the servers attributes including:
        node number
        number of edge weights
        edge weights (per edge)
        number of routing table entries
        rt entries - dest next hop and dist
     */
    static int nodeNum = 0;
    static int numEdgeWeights = 0;
    static int[][] weights = {};
    static int numRtEntries = 0;
    static int[][] rtEntries = {};

    static DvrClient client;

    static class DvrClient{
        int nodeNum;
        int numDvrEntries;
        int [][] entires;

        public DvrClient(String clientData){
            // we're going to split the string line by line to parse it
            String [] splitData = clientData.split("\n");


            int entryIndex = 0;

            for(int i = 0; i<splitData.length; i++){
                if(i==0){
                    nodeNum = Integer.parseInt(splitData[i]);
                }else if (i==1){
                    numDvrEntries = Integer.parseInt(splitData[i]);
                    entires = new int [numDvrEntries][2];
                }else if (i>1){
                    //System.out.println("NUM Entries : " + numDvrEntries);
                    //System.out.println("DVR Entry : " + splitData[i]);
                    String [] splitDvr = splitData[i].split(" ");
                    entires[entryIndex][0]=Integer.parseInt(splitDvr[0]);
                    entires[entryIndex][1]=Integer.parseInt(splitDvr[1]);
                    entryIndex++;
                }
            }
        }

        public void printData(){
            System.out.println("Node num : " + this.nodeNum);
            System.out.println("DVR Entries  : " + this.numDvrEntries);
            for(int i = 0; i<this.entires.length; i++){
                for(int j =0; j<this.entires[i].length; j++){
                    System.out.print(this.entires[i][j]+" ");
                }
                System.out.println(" ");
            }
        }

        public int getNodeNum(){
            return this.nodeNum;
        }

        public int getNumDvrEntries(){
            return this.numDvrEntries;
        }

        public int [] [] getDVREntries(){
            return this.entires;
        }
    }

    public static void main(String argv[]) throws Exception {



        ServerSocket welcomeSocket = new ServerSocket(12121);
        readStartUpFile();
       // printServerConfig();



        /* just so we don't have to send multiple messages, we're going
         to send all of the client data in one message..*/
        String clientData = "";
        while (true) {

            clientData = "";
            Socket connectionSocket = welcomeSocket.accept();

            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            /*DataOutputStream outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());*/
            Stream<String> data = inFromClient.lines();
            Iterator <String> dataItr = data.iterator();
            // printing the data via the iterator

            while(dataItr.hasNext()){
                clientData+=dataItr.next().toString()+"\n";
               // System.out.println(dataItr.next().toString());
            }
            client = new DvrClient(clientData);

            System.out.println("Client data : \n");
            client.printData();
           // int [] [] cleintE = client.getDVREntries();
            updateDVR();
            printRoutingTable();

        }

    }

    public static void printRoutingTable(){
        System.out.println("Routing table :\n");
        for(int i =0; i<rtEntries.length; i++){
            for(int j = 0; j<rtEntries[i].length; j++){
                System.out.print(rtEntries[i][j]+" ");
            }
            System.out.println(" ");
        }
    }
    public static void readStartUpFile() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter a filename for server startup");
        String fileName = sc.nextLine();
        System.out.println("Startup file : " + fileName);
        // code reads the startup file line by line

        int index = 0;
        int edgeWeights = 0;
        //int weights[][] = {}; // initially an empty array
        String line = "";
        int weightIndex = 0;
        int rtInit = 0;

        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(reader);
            // read the file line by line
            while ((line = bufferedReader.readLine()) != null) {


                if(index==1) {
                    nodeNum = Integer.parseInt(line);
                }else if (index == 2) {
                    edgeWeights = Integer.parseInt(line);
                    weights = new int[edgeWeights][2];
                }else if (index>2 && index <=(2+edgeWeights)) {
                    // this will read all of the edge weights and put them
                    // in an array, line by line
                    weights[weightIndex] = new int[2];
                    // now get the line and split it
                    String[] splitWeights = line.split(" ");
                  //  System.out.println("Line : " + line);

                    for (int i = 0; i < splitWeights.length; i++) {
                        //System.out.print(Integer.parseInt(splitWeights[i]));
                        weights[weightIndex][i] = Integer.parseInt(splitWeights[i]);
                    }
                    weightIndex++;
                }else if(index==(2+edgeWeights+1)){
                    // now make this the number of routing table entries
                    numRtEntries = Integer.parseInt(line);
                    rtEntries = new int[numRtEntries][3];
                }else if (index > (2+edgeWeights+1) && index <=(2+edgeWeights+1+numRtEntries)){
                    // we're just going to use an else to capture everything else..
                    rtEntries[rtInit] = new int[3];
                    // split the rt entries on a space
                    String [] rtString = line.split(" ");
                    for(int i = 0; i<rtString.length; i++){
                        rtEntries[rtInit][i]=Integer.parseInt(rtString[i]);
                    }
                    rtInit++;
                }

                index++;


            }
            // now initialize everything to the correct variables
            numEdgeWeights = edgeWeights;

        } catch (FileNotFoundException e) {
            System.out.println("File not found - invalid configuration file");
        } catch (IOException e) {
            System.out.println("Error reading file..");
        }

    }

    public static void printServerConfig(){
        // this prints the current server configuration
        System.out.println("Edge Weights : " + numEdgeWeights);
        for(int i = 0; i<weights.length; i++){
            for(int k = 0; k<weights[i].length; k++) {
                System.out.print(weights[i][k] + " ");
            }
            System.out.println("");
        }

        System.out.println("Num RT entries : " + numRtEntries);
        for(int i = 0; i<rtEntries.length; i++){
            for(int k = 0; k<rtEntries[i].length; k++) {
                System.out.print(rtEntries[i][k] + " ");
            }
            System.out.println("");
        }
    }

    public static void updateDVR(){
        // for each entry in the message
        // get the client entries

        int maxDist = -999;

        int [][] clientEntries = client.getDVREntries();
        // for each entry in the server routing table
        for(int x = 0; x<rtEntries.length; x++) {

            // for each entry in the message
            for (int i = 0; i < clientEntries.length; i++) {
                // let V be the destination in the entry
                int v = clientEntries[i][0];
                // let D be the distance
                int d = clientEntries[i][1];
                // compute C ad D plus the weight of the current link
                int c = d+rtEntries[x][0];

                // now examine and update the local routing table

                // if no route exists
                if(v == rtEntries[x][0]){
                        break;
                }else if(clientEntries[i][0] != rtEntries[x][0]){
                   // now update the table
                    if(rtEntries[x][2] >= maxDist+clientEntries[i][1]){
                        rtEntries[x][i] = v;
                    }
                    rtEntries[x][2] = maxDist+clientEntries[i][1];
                }


            }
        }
    }
}