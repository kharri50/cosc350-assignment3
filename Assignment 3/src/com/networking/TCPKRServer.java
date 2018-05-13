package com.networking;

import javax.swing.text.html.HTMLDocument;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.Set;


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
    static int [][] weights = {};
    static int numRtEntries = 0;
    static int [][] rtEntries = {};

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
            updateDVR(client.getNodeNum());
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

    public static void updateDVR(int nodeDest){
        // for each entry in the message
        // get the client entries


        int [][] clientEntries = client.getDVREntries();
        // for each entry in the server routing table

            // for each entry in the message
            int nodeWeight = 0;
            for (int i = 0; i < clientEntries.length; i++) {
               /* for each entry in the client message, put a new row
                * in the RT table and initially put them all to zero */

               /* If the node doesn't exist in the table, put in a new entry
                   and copy the table
                */

               int dest = clientEntries[i][0];
               int cost = clientEntries[i][1];
               // check if the current table has the destination

                if(inTable(dest)){

                    if (pathExists(dest) && dest == nodeDest) {
                    /* This is a special case */
                        nodeWeight = getNodeWeight(dest);
                    }
                    //System.out.println("In table, not adding!");
                    // now get the cost of the current element
                    int currentCost = rtEntries[getIndex(dest)][2];
                    System.out.println("Node weight : " + nodeWeight);
                    if((cost+nodeWeight) < currentCost){
                        // update the row
                        updateRow(dest, nodeDest, cost+nodeWeight);
                    }

                }else {
                    // if the destination equals a path, set the node weight
                    if (pathExists(dest) && dest == nodeDest) {
                    /* This is a special case */
                        nodeWeight = getNodeWeight(dest);
                        putRow(nodeDest, nodeDest, nodeWeight);

                    } else if (pathExists(dest) && dest == nodeDest) {
                        putRow(dest, dest, nodeWeight);
                    } else {
                        //it doesn't contain the path so put the node weight in
                        putRow(dest, nodeDest, (clientEntries[i][1] + nodeWeight));
                    }
                }
        }

        printRoutingTable();
       // printRoutingTable();

    }

    public static void putRow(int destination, int hop, int weight){
        // copy the entries
        int [] [] tempRt = rtEntries;
        int [] [] newRt = new int[rtEntries.length+1][3];
        int len = tempRt.length;
        for(int i = 0; i<tempRt.length; i++){
            // copy the row
            for(int j = 0; j<tempRt[i].length; j++){
                newRt[i][j] = tempRt[i][j];
            }
        }
        // put in a new row with the destination and next hop
        newRt[len][0]=destination;
        newRt[len][1]=hop;
        newRt[len][2]=weight;
        rtEntries  = newRt;
    }

    public static boolean containsDest(int destination){
        for(int i = 0; i<rtEntries.length; i++){
            if(rtEntries[i][0] == destination){
                return true;
            }
        }
        return false;
    }

    public static boolean pathExists(int dest){
        // check if the server has the destination
        for(int i = 0; i<weights.length; i++){
            if(weights[i][0]==dest){
                return true;
            }
        }
        return false;
    }

    public static int getNodeWeight(int dest){
        for(int i = 0; i<weights.length; i++){
            if(weights[i][0]==dest){
                return weights[i][1];
            }
        }
        return -1; // invalid weight
    }

    public static void updateRow(int dest, int hop, int cost){
       for(int i = 0; i<rtEntries.length; i++){
           if(rtEntries[i][0]==dest){
               // update the hop and cost
               rtEntries[i][1] = hop;
               rtEntries[i][2] = cost;
           }
       }
    }

    public static boolean inTable(int node){
        for(int i  = 0; i<rtEntries.length; i++){
           if(rtEntries[i][0] == node){
               return true;
           }
        }
        return false;
    }

    public static int getIndex(int node){
        for(int i  = 0; i<rtEntries.length; i++){
            if(rtEntries[i][0] == node){
                return i;
            }
        }
        return -1;
    }
}