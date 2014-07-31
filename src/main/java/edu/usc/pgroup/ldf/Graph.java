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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Charith Wickramaarachchi on 7/29/14.
 */
public class Graph {


    Map<String, Integer> tot;

    Map<String, Integer> in;

    Map<String,String> n2c;

    Map<String, List<String>> graph = new HashMap<String, List<String>>();

    long m2 = 0;

    int size = 0;

    Map<String, List<Integer>> weights;

    private int min = Integer.MAX_VALUE;

    private int max = Integer.MIN_VALUE;


    public int degree(String node) {
        return graph.containsKey(node) ? graph.get(node).size() : null;
    }

    public List<String> neighbours(String node) {
        return graph.get(node);
    }


    public Graph(File file, boolean weighted) throws Exception {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line = reader.readLine();

        m2 = 0;
        while (line != null) {

            StringTokenizer tokenizer = new StringTokenizer(line);
            String source = tokenizer.nextToken();
            String sink = tokenizer.nextToken();

            if (graph.containsKey(source)) {
                if (!graph.get(source).contains(sink)) {
                    graph.get(source).add(sink);
                    m2++;
                }
            } else {
                List<String> list = new ArrayList<String>();
                list.add(sink);
                graph.put(source, list);
                m2++;
            }

            line = reader.readLine();

        }

        int size = graph.size();
        tot = new HashMap<String, Integer>(size);
        in = new HashMap<String, Integer>(size);
        n2c = new HashMap<String, String>(size);
        Iterator<String> graphIt = graph.keySet().iterator();

        while (graphIt.hasNext()) {
            String source = graphIt.next();
            int degree = graph.get(source).size();
            tot.put(source, degree);
            in.put(source, 0);
            n2c.put(source,source);
            if (min > degree) {
                min = degree;
            }

            if (max < degree) {
                max = degree;
            }

        }

        System.out.println("Graph : number of nodes = " + graph.size() + " number of edges = " + m2);
        System.out.println("Min degree: " + min + " Max degree:" + max);

    }


    public double modularity() {

        double q = 0.;

        Iterator<String> it = graph.keySet().iterator();

        while (it.hasNext()) {
            String source = it.next();
            if (source != null && tot.containsKey(source) && tot.get(source) > 0) {
                q += (double) in.get(source) / (double) m2 - ((double) tot.get(source) / m2) * ((double) tot.get(source) / m2);
            }
        }

        return q;
    }

    public String[] getNodeListSorted() {

        long[] count = new long[max - min + 1];

        Iterator<String> it = graph.keySet().iterator();

        while (it.hasNext()) {
            String source = it.next();
            count[graph.get(source).size()-1] += 1;
        }

        long total = 0;

        for (int i = 0; i < count.length; i++) {
            long oldCount = count[i];
            count[i] = total;
            total += oldCount;
        }

        String[] array = new String[graph.size()];

        it = graph.keySet().iterator();

        while (it.hasNext()) {
            String source  = it.next();
            array[(int)count[graph.get(source).size() -1]] = source;
            count[graph.get(source).size()-1]++;
        }

        return array;
    }

    public void insertToComm(String node,String community) {
        int d = tot.get(community);
        tot.put(community,d+ degree(node));
        in.put(community,in.get(community) + 2);
        n2c.put(node,community);
    }

    public void remoteFromComm(String node,String community) {

        int d = tot.get(community);
        tot.put(community,d - degree(node));
        in.put(community,in.get(community) - 2);
        n2c.remove(node);

    }
    public int getMinDegree() {
        return min;
    }

    public int getMaxDegree() {
        return max;
    }
}
