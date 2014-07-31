/*
 *  Copyright 2013 University of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.package edu.usc.goffish.gopher.sample;
 */
package edu.usc.pgroup.ldf;

import java.io.File;
import java.util.*;

/**
 * Created by Charith Wickramaarachchi on 7/29/14.
 */
public class A3CS_A_BASE {

    Graph graph;

    final int FOLLOWER = 1;
    final int LEADER = 0;

    Map<String,Integer> label;
    Map<String,String> follow;

    Map<String,List<String>> followers = new HashMap<String, List<String>>();

    public static void main(String[] args) throws Exception{
        String fileName = args[0];

        new A3CS_A_BASE().start(fileName);
    }


    private void start(String filename) throws Exception{
        File file = new File(filename);
        graph = new Graph(file,false);
        System.out.println("Modulatiy : " + graph.modularity());


        label = new HashMap<String, Integer>(graph.size);
        follow = new HashMap<String, String>(graph.size);

        //get the vertices in non decreasing order of degree
        String[] vertices = graph.getNodeListSorted();

        for(String v:vertices) {
            int k = graph.degree(v);
            if( k <= graph.getMaxDegree() && !label.containsKey(v)){
                  follow_neighbour(v);
            }

        }

        System.out.println("Modulatiy : " + graph.modularity());

    }


    private void follow_neighbour(String node) {

        label.put(node,FOLLOWER);

        boolean exit = false;

        List<String> list = graph.neighbours(node);
        for(String s : list) {
            if(!label.containsKey(s) || label.get(s) == LEADER) {
                if(!label.containsKey(s)) {
                    label.put(s,LEADER);
                }

                follow.put(node,s); // node follow s

                graph.remoteFromComm(node,graph.n2c.get(node));
                graph.insertToComm(node,graph.n2c.get(s));

                if(!followers.containsKey(s)) {
                    List<String> l = new ArrayList<String>();
                    l.add(node);
                    followers.put(s,l);
                } else {
                    followers.get(s).add(node);
                }

                exit = true;
                break;
            }
        }

        if(!exit) {

           // String j = list.get(new Random().nextInt(list.size()));
            String j = findBestNeighbourComm(node,list);


            unfollow(j);
            follow.put(node,j);

            graph.remoteFromComm(node,graph.n2c.get(node));
            graph.insertToComm(node,graph.n2c.get(j));


            if(!followers.containsKey(j)) {
                List<String> l = new ArrayList<String>();
                l.add(node);
                followers.put(j,l);
            } else {
                followers.get(j).add(node);
            }


            label.put(j,LEADER);
        }
    }

    private void unfollow(String i){

        String j = follow.get(i);
        label.remove(i);

        graph.remoteFromComm(i,graph.n2c.get(i));
        graph.insertToComm(i,i);


        if(j != null) {
            followers.get(j).remove(i);
        }
        if(!hasFollower(j)) {
            int k = graph.degree(j);
            if(k < graph.getMaxDegree()) {
                follow.put(j,i);

                graph.remoteFromComm(j,graph.n2c.get(j));
                graph.insertToComm(j,graph.n2c.get(i));

                label.put(j,FOLLOWER);
                label.put(i,LEADER);
            } else {
                label.remove(j);
            }

        }



    }

    private boolean hasFollower(String i) {
        return followers.containsKey(i) && followers.get(i).size() > 0;
    }




    private String findBestNeighbourComm(String node,List<String> neighbours) {

        String best = null;
        double currentMax = Double.MIN_VALUE;
        for(String n : neighbours) {
            String comm = graph.n2c.get(node);

            double totc = (double) graph.tot.get(comm);
            double degc = (double) graph.degree(node);
            double m2 = (double) graph.m2;
            double dnc = (double) 1;

            double gain = (dnc - totc * degc / m2);

            if(gain > currentMax) {
                currentMax = gain;
                best = n;
            }
        }

        return best;
    }







}
