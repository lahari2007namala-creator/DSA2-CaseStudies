// Hospital Infrastructure Monitoring using BFS
// KLEF DSA-2 | CO2 Case Study

import java.util.*;

public class HospitalBFS {

    // ── Graph (Adjacency List) ────────────────────────────────────────────
    static int        V;                        // number of nodes
    static String[]   dept;                     // department names
    static List<Integer>[] adj;                 // adjacency list

    @SuppressWarnings("unchecked")
    static void initGraph(int v, String[] departments) {
        V    = v;
        dept = departments;
        adj  = new ArrayList[V];
        for (int i = 0; i < V; i++) adj[i] = new ArrayList<>();
    }

    static void addEdge(int u, int v) {
        adj[u].add(v);
        adj[v].add(u);   // undirected – corridor goes both ways
    }

    // ── BFS – core traversal ──────────────────────────────────────────────
    // Returns parent[] array so any path can be reconstructed
    static int[] bfs(int src, boolean print) {
        boolean[] visited = new boolean[V];
        int[]     parent  = new int[V];
        int[]     level   = new int[V];
        Arrays.fill(parent, -1);
        Arrays.fill(level,  -1);

        Queue<Integer> queue = new LinkedList<>();
        visited[src] = true;
        level[src]   = 0;
        queue.add(src);

        // Group nodes by BFS level for neat display
        Map<Integer, List<String>> levelMap = new LinkedHashMap<>();

        while (!queue.isEmpty()) {
            int u = queue.poll();
            levelMap.computeIfAbsent(level[u], k -> new ArrayList<>()).add(dept[u]);

            for (int nb : adj[u]) {
                if (!visited[nb]) {
                    visited[nb] = true;
                    parent[nb]  = u;
                    level[nb]   = level[u] + 1;
                    queue.add(nb);
                }
            }
        }

        if (print) {
            System.out.println("  BFS Level-wise traversal from [" + dept[src] + "]:");
            for (Map.Entry<Integer, List<String>> entry : levelMap.entrySet())
                System.out.println("    Level " + entry.getKey() + " : " + entry.getValue());
        }
        return parent;
    }

    // ── Shortest path between two departments ─────────────────────────────
    static void shortestPath(int src, int dest) {
        int[] parent = bfs(src, false);
        if (parent[dest] == -1 && src != dest) {
            System.out.println("  [NO PATH]  " + dept[src] + " -> " + dept[dest]);
            return;
        }
        // Reconstruct path
        List<String> path = new ArrayList<>();
        for (int v = dest; v != -1; v = parent[v]) path.add(dept[v]);
        Collections.reverse(path);
        System.out.println("  Shortest Route  : " + String.join(" -> ", path));
        System.out.println("  Hops (distance) : " + (path.size() - 1));
    }

    // ── Connected departments reachable from a source ─────────────────────
    static void reachableFrom(int src) {
        boolean[] visited = new boolean[V];
        Queue<Integer> queue = new LinkedList<>();
        visited[src] = true;
        queue.add(src);
        List<String> reachable = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (u != src) reachable.add(dept[u]);
            for (int nb : adj[u]) {
                if (!visited[nb]) { visited[nb] = true; queue.add(nb); }
            }
        }
        System.out.println("  Departments reachable from [" + dept[src] + "] : " + reachable);
        System.out.println("  Total reachable : " + reachable.size());
    }

    // ── Level-wise monitoring (BFS from a hub) ────────────────────────────
    static void levelWiseMonitoring(int src) {
        boolean[] visited = new boolean[V];
        int[]     level   = new int[V];
        Arrays.fill(level, -1);
        Queue<Integer> queue = new LinkedList<>();
        visited[src] = true;
        level[src]   = 0;
        queue.add(src);

        Map<Integer, List<String>> levelMap = new LinkedHashMap<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            levelMap.computeIfAbsent(level[u], k -> new ArrayList<>()).add(dept[u]);
            for (int nb : adj[u]) {
                if (!visited[nb]) {
                    visited[nb] = true;
                    level[nb]   = level[u] + 1;
                    queue.add(nb);
                }
            }
        }
        System.out.println("  Level-wise hospital monitoring from [" + dept[src] + "]:");
        for (Map.Entry<Integer, List<String>> e : levelMap.entrySet())
            System.out.printf("    Level %d (%2d dept) : %s%n",
                e.getKey(), e.getValue().size(), e.getValue());
    }

    // ── Check direct connectivity between two departments ─────────────────
    static void checkConnectivity(int u, int v) {
        boolean direct = adj[u].contains(v);
        System.out.println("  [" + dept[u] + "]  <-->  [" + dept[v] + "] : " +
                           (direct ? "DIRECTLY CONNECTED" : "NOT directly connected"));
    }

    // ── Patient transfer path ─────────────────────────────────────────────
    static void patientTransfer(int src, int dest) {
        System.out.println("  Patient Transfer: [" + dept[src] + "] -> [" + dept[dest] + "]");
        shortestPath(src, dest);
    }

    // ── DFS helper (for comparison) ───────────────────────────────────────
    static boolean[] dfsVisited;
    static List<String> dfsOrder;
    static void dfs(int u) {
        dfsVisited[u] = true;
        dfsOrder.add(dept[u]);
        for (int nb : adj[u])
            if (!dfsVisited[nb]) dfs(nb);
    }

    // ── Adjacency list display ─────────────────────────────────────────────
    static void printGraph() {
        System.out.println("  Hospital Facility Graph (Adjacency List):");
        for (int i = 0; i < V; i++) {
            List<String> neighbors = new ArrayList<>();
            for (int nb : adj[i]) neighbors.add(dept[nb]);
            System.out.printf("    [%2d] %-28s -> %s%n", i, dept[i], neighbors);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  MAIN
    // ═════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {

        // ── Department node definitions ───────────────────────────────────
        // Node IDs:
        //  0 = Main Entrance       1 = Emergency Ward     2 = Triage Room
        //  3 = ICU                 4 = Operation Theater  5 = Recovery Room
        //  6 = General Ward A      7 = General Ward B     8 = Pediatric Ward
        //  9 = Pharmacy           10 = Laboratory        11 = Radiology
        // 12 = Blood Bank         13 = Ambulance Bay     14 = Cafeteria
        // 15 = Admin Office

        String[] departments = {
            "Main Entrance",    // 0
            "Emergency Ward",   // 1
            "Triage Room",      // 2
            "ICU",              // 3
            "Operation Theater",// 4
            "Recovery Room",    // 5
            "General Ward A",   // 6
            "General Ward B",   // 7
            "Pediatric Ward",   // 8
            "Pharmacy",         // 9
            "Laboratory",       // 10
            "Radiology",        // 11
            "Blood Bank",       // 12
            "Ambulance Bay",    // 13
            "Cafeteria",        // 14
            "Admin Office"      // 15
        };

        initGraph(16, departments);

        // ── Hospital corridor connections (edges) ─────────────────────────
        addEdge(0,  1);  // Main Entrance   <-> Emergency Ward
        addEdge(0,  9);  // Main Entrance   <-> Pharmacy
        addEdge(0, 15);  // Main Entrance   <-> Admin Office
        addEdge(0, 14);  // Main Entrance   <-> Cafeteria
        addEdge(1,  2);  // Emergency Ward  <-> Triage Room
        addEdge(1,  3);  // Emergency Ward  <-> ICU
        addEdge(1, 13);  // Emergency Ward  <-> Ambulance Bay
        addEdge(2,  6);  // Triage Room     <-> General Ward A
        addEdge(2, 10);  // Triage Room     <-> Laboratory
        addEdge(3,  4);  // ICU             <-> Operation Theater
        addEdge(3, 12);  // ICU             <-> Blood Bank
        addEdge(4,  5);  // Operation Theater <-> Recovery Room
        addEdge(5,  6);  // Recovery Room   <-> General Ward A
        addEdge(6,  7);  // General Ward A  <-> General Ward B
        addEdge(6,  9);  // General Ward A  <-> Pharmacy
        addEdge(7,  8);  // General Ward B  <-> Pediatric Ward
        addEdge(7,  9);  // General Ward B  <-> Pharmacy
        addEdge(10, 11); // Laboratory      <-> Radiology
        addEdge(10, 12); // Laboratory      <-> Blood Bank
        addEdge(11,  3); // Radiology       <-> ICU
        addEdge(13,  1); // Ambulance Bay   <-> Emergency Ward (already added, kept for clarity)
        addEdge(14, 15); // Cafeteria       <-> Admin Office

        System.out.println("============================================================");
        System.out.println("  Hospital Infrastructure Monitoring using BFS");
        System.out.println("  Nodes (Departments) : " + V);
        System.out.println("============================================================\n");

        // ── 1. Hospital Graph ─────────────────────────────────────────────
        System.out.println("--- 1. Hospital Facility Graph ---");
        printGraph();

        // ── 2. Full BFS from Main Entrance ────────────────────────────────
        System.out.println("\n--- 2. BFS Traversal from Main Entrance ---");
        bfs(0, true);

        // ── 3. Level-wise Monitoring from Emergency Ward ──────────────────
        System.out.println("\n--- 3. Level-wise Monitoring from Emergency Ward ---");
        levelWiseMonitoring(1);

        // ── 4. Emergency Route Analysis ───────────────────────────────────
        System.out.println("\n--- 4. Emergency Route Analysis ---");

        System.out.println("  4a. Ambulance Bay -> ICU (critical patient):");
        shortestPath(13, 3);

        System.out.println("  4b. Main Entrance -> Operation Theater (emergency surgery):");
        shortestPath(0, 4);

        System.out.println("  4c. Emergency Ward -> Blood Bank (urgent transfusion):");
        shortestPath(1, 12);

        System.out.println("  4d. Ambulance Bay -> Operation Theater (direct trauma):");
        shortestPath(13, 4);

        // ── 5. Patient Transfer Tracking ─────────────────────────────────
        System.out.println("\n--- 5. Patient Transfer Tracking ---");

        System.out.println("  5a. ICU -> Recovery Room (post-surgery transfer):");
        patientTransfer(3, 5);

        System.out.println("  5b. General Ward A -> Laboratory (lab test):");
        patientTransfer(6, 10);

        System.out.println("  5c. Pediatric Ward -> Pharmacy (medicine pickup):");
        patientTransfer(8, 9);

        // ── 6. Laboratory Connectivity Analysis ──────────────────────────
        System.out.println("\n--- 6. Laboratory Connectivity Analysis ---");
        System.out.println("  Departments reachable from Laboratory:");
        reachableFrom(10);

        // ── 7. ICU Connectivity Analysis ─────────────────────────────────
        System.out.println("\n--- 7. ICU Connectivity Analysis ---");
        System.out.println("  Departments reachable from ICU:");
        reachableFrom(3);

        // ── 8. Doctor / Staff Accessibility ──────────────────────────────
        System.out.println("\n--- 8. Doctor Accessibility Monitoring ---");
        System.out.println("  8a. Can doctor reach ICU from Admin Office?");
        shortestPath(15, 3);
        System.out.println("  8b. Can doctor reach Radiology from Cafeteria?");
        shortestPath(14, 11);

        // ── 9. Direct Connectivity Check ─────────────────────────────────
        System.out.println("\n--- 9. Direct Connectivity Check ---");
        checkConnectivity(1, 3);   // Emergency Ward <-> ICU
        checkConnectivity(4, 6);   // Operation Theater <-> General Ward A
        checkConnectivity(13, 4);  // Ambulance Bay <-> Operation Theater
        checkConnectivity(0, 10);  // Main Entrance <-> Laboratory

        // ── 10. BFS from Ambulance Bay (emergency hub) ───────────────────
        System.out.println("\n--- 10. BFS Level-wise from Ambulance Bay ---");
        bfs(13, true);

        // ── 11. BFS vs DFS Comparison ─────────────────────────────────────
        System.out.println("\n--- 11. BFS vs DFS Comparison ---");

        System.out.print("  BFS order from Emergency Ward : ");
        boolean[] bfsVisited = new boolean[V];
        Queue<Integer> bfsQ  = new LinkedList<>();
        List<String> bfsOrder = new ArrayList<>();
        bfsVisited[1] = true; bfsQ.add(1);
        while (!bfsQ.isEmpty()) {
            int u = bfsQ.poll(); bfsOrder.add(dept[u]);
            for (int nb : adj[u]) if (!bfsVisited[nb]) { bfsVisited[nb]=true; bfsQ.add(nb); }
        }
        System.out.println(bfsOrder);

        System.out.print("  DFS order from Emergency Ward : ");
        dfsVisited = new boolean[V];
        dfsOrder   = new ArrayList<>();
        dfs(1);
        System.out.println(dfsOrder);

        System.out.println("\n  Comparison Summary:");
        System.out.println("  +---------------------+----------------------------+----------------------------+");
        System.out.println("  | Property            | BFS                        | DFS                        |");
        System.out.println("  +---------------------+----------------------------+----------------------------+");
        System.out.println("  | Data Structure      | Queue (FIFO)               | Stack / Recursion          |");
        System.out.println("  | Traversal Order     | Level by level             | Deep branch first          |");
        System.out.println("  | Shortest Path       | YES (unweighted)           | NO                         |");
        System.out.println("  | Emergency Navigation| OPTIMAL                    | Not optimal                |");
        System.out.println("  | Memory Usage        | O(V) queue                 | O(V) stack                 |");
        System.out.println("  | Time Complexity     | O(V + E)                   | O(V + E)                   |");
        System.out.println("  | Space Complexity    | O(V)                       | O(V)                       |");
        System.out.println("  | Best Use Case       | Shortest path, level scan  | Connected components, maze |");
        System.out.println("  +---------------------+----------------------------+----------------------------+");

        // ── 12. Time Complexity Summary ───────────────────────────────────
        System.out.println("\n--- 12. Time Complexity Analysis ---");
        System.out.println("  BFS Traversal          : O(V + E)");
        System.out.println("  Shortest Path (BFS)    : O(V + E)");
        System.out.println("  Reachability Check     : O(V + E)");
        System.out.println("  Connectivity Check     : O(1)  (adjacency list lookup)");
        System.out.println("  Space Complexity       : O(V)  (visited array + queue)");
        System.out.println("  V = " + V + " departments,  E = 21 corridors");
        System.out.println("  => BFS guarantees shortest hop-path for emergency navigation.");

        System.out.println("\n============================================================");
        System.out.println("  Process finished with exit code 0");
        System.out.println("============================================================");
    }
}
