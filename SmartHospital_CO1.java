// Smart Hospital Management System using BST
// KLEF DSA-2 | CO1 Case Study

public class HospitalBST {

    // ── Node ──────────────────────────────────────────────────────────────
    static class BSTNode {
        int     patientId;
        String  patientName;
        String  disease;
        int     roomNumber;
        String  doctorName;
        String  labTest;
        BSTNode left, right;

        BSTNode(int id, String name, String disease, int room,
                String doctor, String lab) {
            this.patientId   = id;
            this.patientName = name;
            this.disease     = disease;
            this.roomNumber  = room;
            this.doctorName  = doctor;
            this.labTest     = lab;
            this.left = this.right = null;
        }
    }

    static BSTNode root          = null;
    static int     dischargedCount = 0;

    // ── Insert ────────────────────────────────────────────────────────────
    static BSTNode insert(BSTNode node, int id, String name, String disease,
                          int room, String doctor, String lab) {
        if (node == null) {
            System.out.println("  [REGISTERED] PID: " + id + " | " + name +
                               " | Room: " + room);
            return new BSTNode(id, name, disease, room, doctor, lab);
        }
        if (id < node.patientId)
            node.left  = insert(node.left,  id, name, disease, room, doctor, lab);
        else if (id > node.patientId)
            node.right = insert(node.right, id, name, disease, room, doctor, lab);
        else
            System.out.println("  [DUPLICATE]  PID " + id + " already exists.");
        return node;
    }

    // ── Search ────────────────────────────────────────────────────────────
    static BSTNode search(BSTNode node, int id) {
        if (node == null || node.patientId == id) return node;
        return id < node.patientId
               ? search(node.left,  id)
               : search(node.right, id);
    }

    // ── Find minimum node (used by delete) ────────────────────────────────
    static BSTNode minNode(BSTNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    // ── Delete ────────────────────────────────────────────────────────────
    static BSTNode delete(BSTNode node, int id) {
        if (node == null) {
            System.out.println("  [NOT FOUND]  PID " + id + " does not exist.");
            return null;
        }
        if (id < node.patientId) {
            node.left  = delete(node.left,  id);
        } else if (id > node.patientId) {
            node.right = delete(node.right, id);
        } else {
            System.out.println("  [DISCHARGED] PID: " + id + " | " +
                               node.patientName + " | Room " +
                               node.roomNumber + " now free.");
            dischargedCount++;
            if (node.left  == null) return node.right;
            if (node.right == null) return node.left;
            // Two children: replace with in-order successor
            BSTNode succ     = minNode(node.right);
            node.patientId   = succ.patientId;
            node.patientName = succ.patientName;
            node.disease     = succ.disease;
            node.roomNumber  = succ.roomNumber;
            node.doctorName  = succ.doctorName;
            node.labTest     = succ.labTest;
            node.right       = delete(node.right, succ.patientId);
        }
        return node;
    }

    // ── In-order Traversal (sorted by Patient ID) ─────────────────────────
    static void inorderTraversal(BSTNode node) {
        if (node == null) return;
        inorderTraversal(node.left);
        System.out.printf(
            "  PID:%-5d  %-18s  Disease:%-20s  Room:%-4d  Doctor:%-15s  Lab:%s%n",
            node.patientId, node.patientName, node.disease,
            node.roomNumber, node.doctorName, node.labTest);
        inorderTraversal(node.right);
    }

    // ── Room Allocation Display ───────────────────────────────────────────
    static void roomAllocation(BSTNode node) {
        if (node == null) return;
        roomAllocation(node.left);
        System.out.printf("  Room %-4d  ->  Patient: %-18s  Disease: %s%n",
            node.roomNumber, node.patientName, node.disease);
        roomAllocation(node.right);
    }

    // ── Appointment Scheduling Display ────────────────────────────────────
    static void appointmentSchedule(BSTNode node) {
        if (node == null) return;
        appointmentSchedule(node.left);
        System.out.printf("  PID:%-5d  %-18s  ->  Dr. %-15s  Lab: %s%n",
            node.patientId, node.patientName, node.doctorName, node.labTest);
        appointmentSchedule(node.right);
    }

    // ── Hospital Analytics ────────────────────────────────────────────────
    static int countNodes(BSTNode node) {
        if (node == null) return 0;
        return 1 + countNodes(node.left) + countNodes(node.right);
    }

    static int treeHeight(BSTNode node) {
        if (node == null) return 0;
        return 1 + Math.max(treeHeight(node.left), treeHeight(node.right));
    }

    static void hospitalAnalytics() {
        int active = countNodes(root);
        System.out.println("  Total Registered (incl. discharged) : " + (active + dischargedCount));
        System.out.println("  Currently Active Patients            : " + active);
        System.out.println("  Discharged Patients                  : " + dischargedCount);
        System.out.println("  BST Height (max search steps)        : " + treeHeight(root));
        System.out.println("  Rooms Currently Occupied             : " + active);
        System.out.printf ("  Hospital Utilisation (out of 20)     : %.1f%%%n",
                            (active / 20.0) * 100);
    }

    // ── Main ──────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        System.out.println("============================================================");
        System.out.println("     Smart Hospital Management System using BST");
        System.out.println("============================================================\n");

        // ── 1. Patient Registration ──────────────────────────────────────
        System.out.println("--- 1. Patient Registration ---");
        Object[][] patients = {
            {1050, "Ravi Kumar",   "Diabetes",     101, "Dr. Sharma",  "Blood Sugar"},
            {1030, "Priya Reddy",  "Hypertension", 102, "Dr. Mehta",   "ECG"},
            {1070, "Anita Singh",  "Fracture",     103, "Dr. Rao",     "X-Ray"},
            {1020, "Vikram Naidu", "Fever",        104, "Dr. Patel",   "CBC"},
            {1060, "Sneha Patel",  "Asthma",       105, "Dr. Gupta",   "Spirometry"},
            {1010, "Karan Mehta",  "Malaria",      106, "Dr. Reddy",   "Blood Smear"},
            {1080, "Divya Thomas", "Appendicitis", 107, "Dr. Iyer",    "Ultrasound"},
            {1040, "Suresh Rao",   "Typhoid",      108, "Dr. Verma",   "Widal Test"},
            {1090, "Meena Laxmi",  "Migraine",     109, "Dr. Joshi",   "MRI"},
            {1025, "Arjun Verma",  "Dengue",       110, "Dr. Nair",    "Platelet Count"},
            {1065, "Pooja Sharma", "Pneumonia",    111, "Dr. Das",     "Chest X-Ray"},
            {1035, "Rohit Das",    "Kidney Stone", 112, "Dr. Pillai",  "Ultrasound"},
        };
        for (Object[] p : patients)
            root = insert(root, (int)p[0], (String)p[1], (String)p[2],
                          (int)p[3], (String)p[4], (String)p[5]);

        // ── 2. In-order Traversal ────────────────────────────────────────
        System.out.println("\n--- 2. All Patient Records (Sorted by Patient ID) ---");
        inorderTraversal(root);

        // ── 3. Search Patient ────────────────────────────────────────────
        System.out.println("\n--- 3. Search Patient Records ---");
        int[] searchIds = {1060, 1035, 1099};
        for (int id : searchIds) {
            BSTNode found = search(root, id);
            if (found != null)
                System.out.printf(
                    "  [FOUND] PID:%d | %-18s | Disease: %-15s | Room: %d | Dr: %s%n",
                    found.patientId, found.patientName,
                    found.disease, found.roomNumber, found.doctorName);
            else
                System.out.println("  [NOT FOUND] PID " + id + " does not exist.");
        }

        // ── 4. Room Allocation ────────────────────────────────────────────
        System.out.println("\n--- 4. Room Allocation Status ---");
        roomAllocation(root);

        // ── 5. Appointment Scheduling ─────────────────────────────────────
        System.out.println("\n--- 5. Appointment & Lab Test Schedule ---");
        appointmentSchedule(root);

        // ── 6. Delete Discharged Patients ─────────────────────────────────
        System.out.println("\n--- 6. Discharging Patients ---");
        int[] discharge = {1020, 1080, 9999};
        for (int id : discharge)
            root = delete(root, id);

        // ── 7. Records After Discharge ────────────────────────────────────
        System.out.println("\n--- 7. Patient Records After Discharge (Sorted) ---");
        inorderTraversal(root);

        // ── 8. Hospital Analytics ─────────────────────────────────────────
        System.out.println("\n--- 8. Hospital Analytics Report ---");
        hospitalAnalytics();

        // ── 9. Time Complexity ────────────────────────────────────────────
        System.out.println("\n--- 9. Time Complexity Analysis ---");
        System.out.println("  BST Insert / Search / Delete : O(log n) avg | O(n) worst");
        System.out.println("  In-order Traversal           : O(n)");
        System.out.println("  ArrayList Search             : O(n)");
        System.out.println("  => BST is faster for large-scale hospital record management.");

        System.out.println("\n============================================================");
        System.out.println("  Process finished with exit code 0");
        System.out.println("============================================================");
    }
}
