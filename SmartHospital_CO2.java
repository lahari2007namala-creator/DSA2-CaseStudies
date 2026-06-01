// Smart Hospital Record Management System using B+ Tree
// KLEF DSA-2 | CO1 Case Study

import java.util.*;

public class HospitalBPlusTree {

    static final int ORDER = 4; // max children per internal node

    // ── Patient Record ────────────────────────────────────────────────────
    static class PatientRecord {
        int    patientId;
        String patientName;
        String disease;
        int    roomNumber;
        String doctorName;
        String labReport;
        String appointmentDate;
        String status; // ADMITTED / DISCHARGED

        PatientRecord(int id, String name, String disease, int room,
                      String doctor, String lab, String date) {
            this.patientId       = id;
            this.patientName     = name;
            this.disease         = disease;
            this.roomNumber      = room;
            this.doctorName      = doctor;
            this.labReport       = lab;
            this.appointmentDate = date;
            this.status          = "ADMITTED";
        }

        public String toString() {
            return String.format(
                "PID:%-5d  %-18s  Disease:%-18s  Room:%-4d  Dr:%-14s  Lab:%-16s  Date:%-12s  [%s]",
                patientId, patientName, disease, roomNumber,
                doctorName, labReport, appointmentDate, status);
        }
    }

    // ── B+ Tree Node ──────────────────────────────────────────────────────
    static class BPlusNode {
        boolean              isLeaf;
        List<Integer>        keys;
        List<BPlusNode>      children;   // internal nodes only
        List<PatientRecord>  records;    // leaf nodes only
        BPlusNode            next;       // leaf linked list pointer

        BPlusNode(boolean isLeaf) {
            this.isLeaf   = isLeaf;
            this.keys     = new ArrayList<>();
            this.children = new ArrayList<>();
            this.records  = new ArrayList<>();
            this.next     = null;
        }
    }

    // ── B+ Tree ───────────────────────────────────────────────────────────
    static class BPlusTree {

        BPlusNode root;
        int       totalInserted  = 0;
        int       totalDischarged = 0;

        BPlusTree() {
            root = new BPlusNode(true); // start with empty leaf
        }

        // ── Insert ────────────────────────────────────────────────────────
        void insert(PatientRecord rec) {
            totalInserted++;
            SplitResult result = insertRec(root, rec);
            if (result != null) {
                // Root was split – create a new root
                BPlusNode newRoot = new BPlusNode(false);
                newRoot.keys.add(result.promoteKey);
                newRoot.children.add(root);
                newRoot.children.add(result.newNode);
                root = newRoot;
                System.out.println("  [ROOT SPLIT]   New root created with key " + result.promoteKey);
            }
            System.out.println("  [INSERTED]     " + rec);
        }

        // Returns non-null SplitResult if the node overflowed and was split
        SplitResult insertRec(BPlusNode node, PatientRecord rec) {
            if (node.isLeaf) {
                // Insert in sorted position
                int pos = 0;
                while (pos < node.keys.size() && node.keys.get(pos) < rec.patientId) pos++;
                node.keys.add(pos, rec.patientId);
                node.records.add(pos, rec);

                if (node.keys.size() < ORDER) return null; // no overflow
                return splitLeaf(node);
            } else {
                // Find correct child
                int pos = node.keys.size();
                for (int i = 0; i < node.keys.size(); i++) {
                    if (rec.patientId < node.keys.get(i)) { pos = i; break; }
                }
                SplitResult result = insertRec(node.children.get(pos), rec);
                if (result == null) return null;

                // Child split – absorb the promoted key
                int insertPos = 0;
                while (insertPos < node.keys.size() &&
                       node.keys.get(insertPos) < result.promoteKey) insertPos++;
                node.keys.add(insertPos, result.promoteKey);
                node.children.add(insertPos + 1, result.newNode);

                if (node.keys.size() < ORDER) return null;
                return splitInternal(node);
            }
        }

        // Split a full leaf node
        SplitResult splitLeaf(BPlusNode leaf) {
            int mid = ORDER / 2;
            BPlusNode newLeaf = new BPlusNode(true);

            newLeaf.keys.addAll(leaf.keys.subList(mid, leaf.keys.size()));
            newLeaf.records.addAll(leaf.records.subList(mid, leaf.records.size()));

            leaf.keys.subList(mid, leaf.keys.size()).clear();
            leaf.records.subList(mid, leaf.records.size()).clear();

            // Maintain leaf linked list
            newLeaf.next = leaf.next;
            leaf.next    = newLeaf;

            System.out.println("  [LEAF SPLIT]   Promoted key: " + newLeaf.keys.get(0));
            return new SplitResult(newLeaf.keys.get(0), newLeaf);
        }

        // Split a full internal node
        SplitResult splitInternal(BPlusNode node) {
            int mid = ORDER / 2;
            int promoteKey = node.keys.get(mid);

            BPlusNode newInternal = new BPlusNode(false);
            newInternal.keys.addAll(node.keys.subList(mid + 1, node.keys.size()));
            newInternal.children.addAll(node.children.subList(mid + 1, node.children.size()));

            node.keys.subList(mid, node.keys.size()).clear();
            node.children.subList(mid + 1, node.children.size()).clear();

            System.out.println("  [INTERNAL SPLIT] Promoted key: " + promoteKey);
            return new SplitResult(promoteKey, newInternal);
        }

        // ── Search ────────────────────────────────────────────────────────
        PatientRecord search(int patientId) {
            BPlusNode node = root;
            while (!node.isLeaf) {
                int pos = node.keys.size();
                for (int i = 0; i < node.keys.size(); i++) {
                    if (patientId < node.keys.get(i)) { pos = i; break; }
                }
                node = node.children.get(pos);
            }
            for (int i = 0; i < node.keys.size(); i++) {
                if (node.keys.get(i) == patientId) return node.records.get(i);
            }
            return null;
        }

        // ── Range Search (uses leaf linked list) ──────────────────────────
        List<PatientRecord> rangeSearch(int low, int high) {
            List<PatientRecord> result = new ArrayList<>();
            // Find the leaf containing 'low'
            BPlusNode node = root;
            while (!node.isLeaf) {
                int pos = node.keys.size();
                for (int i = 0; i < node.keys.size(); i++) {
                    if (low < node.keys.get(i)) { pos = i; break; }
                }
                node = node.children.get(pos);
            }
            // Scan through linked leaves
            while (node != null) {
                for (int i = 0; i < node.keys.size(); i++) {
                    int key = node.keys.get(i);
                    if (key >= low && key <= high) result.add(node.records.get(i));
                    if (key > high) return result;
                }
                node = node.next;
            }
            return result;
        }

        // ── Update patient status (discharge) ─────────────────────────────
        boolean discharge(int patientId) {
            PatientRecord rec = search(patientId);
            if (rec == null) {
                System.out.println("  [NOT FOUND]  PID " + patientId + " does not exist.");
                return false;
            }
            rec.status = "DISCHARGED";
            totalDischarged++;
            System.out.println("  [DISCHARGED] PID:" + patientId +
                               " | " + rec.patientName +
                               " | Room " + rec.roomNumber + " now free.");
            return true;
        }

        // ── Sequential Access via leaf linked list ─────────────────────────
        void sequentialScan() {
            BPlusNode node = root;
            while (!node.isLeaf) node = node.children.get(0);
            System.out.println("  Sequential scan through linked leaf nodes:");
            int count = 0;
            while (node != null) {
                for (PatientRecord rec : node.records) {
                    System.out.println("    " + rec);
                    count++;
                }
                if (node.next != null) System.out.print("    [-> next leaf] ");
                node = node.next;
            }
            System.out.println("\n  Total records scanned: " + count);
        }

        // ── Print B+ Tree structure ────────────────────────────────────────
        void printTree() {
            System.out.println("  B+ Tree Structure (Level-order):");
            Queue<BPlusNode> queue = new LinkedList<>();
            queue.add(root);
            int level = 0;
            while (!queue.isEmpty()) {
                int size = queue.size();
                System.out.print("    Level " + level + ": ");
                for (int i = 0; i < size; i++) {
                    BPlusNode node = queue.poll();
                    System.out.print(node.isLeaf ? "L" : "I");
                    System.out.print(node.keys + " ");
                    if (!node.isLeaf) queue.addAll(node.children);
                }
                System.out.println();
                level++;
            }
        }

        // ── Hospital Analytics ─────────────────────────────────────────────
        void analytics() {
            int active = totalInserted - totalDischarged;
            System.out.println("  Total Patients Registered : " + totalInserted);
            System.out.println("  Currently Admitted        : " + active);
            System.out.println("  Discharged                : " + totalDischarged);
            System.out.println("  B+ Tree Order             : " + ORDER);
            System.out.println("  Rooms Occupied            : " + active);
            System.out.printf ("  Hospital Utilisation      : %.1f%% (out of 20 rooms)%n",
                                (active / 20.0) * 100);
        }
    }

    // ── Split Result helper ───────────────────────────────────────────────
    static class SplitResult {
        int       promoteKey;
        BPlusNode newNode;
        SplitResult(int key, BPlusNode node) {
            this.promoteKey = key;
            this.newNode    = node;
        }
    }

    // ── Main ──────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        BPlusTree tree = new BPlusTree();

        System.out.println("============================================================");
        System.out.println("  Smart Hospital Record Management System using B+ Tree");
        System.out.println("  B+ Tree Order = " + ORDER);
        System.out.println("============================================================\n");

        // ── 1. Insert Patient Records ─────────────────────────────────────
        System.out.println("--- 1. Inserting Patient Records ---");
        Object[][] patients = {
            {1050, "Ravi Kumar",    "Diabetes",      101, "Dr. Sharma",  "Blood Sugar",   "02-Jun-2025"},
            {1030, "Priya Reddy",   "Hypertension",  102, "Dr. Mehta",   "ECG",           "03-Jun-2025"},
            {1070, "Anita Singh",   "Fracture",      103, "Dr. Rao",     "X-Ray",         "04-Jun-2025"},
            {1020, "Vikram Naidu",  "Fever",         104, "Dr. Patel",   "CBC",           "05-Jun-2025"},
            {1060, "Sneha Patel",   "Asthma",        105, "Dr. Gupta",   "Spirometry",    "06-Jun-2025"},
            {1010, "Karan Mehta",   "Malaria",       106, "Dr. Reddy",   "Blood Smear",   "07-Jun-2025"},
            {1080, "Divya Thomas",  "Appendicitis",  107, "Dr. Iyer",    "Ultrasound",    "08-Jun-2025"},
            {1040, "Suresh Rao",    "Typhoid",       108, "Dr. Verma",   "Widal Test",    "09-Jun-2025"},
            {1090, "Meena Laxmi",   "Migraine",      109, "Dr. Joshi",   "MRI",           "10-Jun-2025"},
            {1025, "Arjun Verma",   "Dengue",        110, "Dr. Nair",    "Platelet Count","11-Jun-2025"},
            {1065, "Pooja Sharma",  "Pneumonia",     111, "Dr. Das",     "Chest X-Ray",   "12-Jun-2025"},
            {1035, "Rohit Das",     "Kidney Stone",  112, "Dr. Pillai",  "Ultrasound",    "13-Jun-2025"},
        };

        for (Object[] p : patients)
            tree.insert(new PatientRecord(
                (int)p[0], (String)p[1], (String)p[2], (int)p[3],
                (String)p[4], (String)p[5], (String)p[6]));

        // ── 2. B+ Tree Structure ──────────────────────────────────────────
        System.out.println("\n--- 2. B+ Tree Structure ---");
        tree.printTree();

        // ── 3. Search Patient Records ─────────────────────────────────────
        System.out.println("\n--- 3. Search Patient Records ---");
        int[] searchIds = {1060, 1035, 1099};
        for (int id : searchIds) {
            PatientRecord rec = tree.search(id);
            if (rec != null)
                System.out.println("  [FOUND]     " + rec);
            else
                System.out.println("  [NOT FOUND] PID " + id + " does not exist.");
        }

        // ── 4. Range Search (leaf linked list traversal) ──────────────────
        System.out.println("\n--- 4. Range Search: PID 1030 to 1070 ---");
        List<PatientRecord> range = tree.rangeSearch(1030, 1070);
        for (PatientRecord rec : range)
            System.out.println("  " + rec);
        System.out.println("  Records found in range: " + range.size());

        // ── 5. Sequential Access via Leaf Linked List ─────────────────────
        System.out.println("\n--- 5. Sequential Access (Leaf Linked List) ---");
        tree.sequentialScan();

        // ── 6. Discharge Patients (Dynamic Update) ────────────────────────
        System.out.println("\n--- 6. Dynamic Updates: Patient Discharge ---");
        int[] discharge = {1020, 1080, 9999};
        for (int id : discharge) tree.discharge(id);

        // ── 7. Verify Updated Records ─────────────────────────────────────
        System.out.println("\n--- 7. Updated Sequential Scan After Discharge ---");
        tree.sequentialScan();

        // ── 8. Hospital Analytics ─────────────────────────────────────────
        System.out.println("\n--- 8. Hospital Analytics Report ---");
        tree.analytics();

        // ── 9. Time Complexity ────────────────────────────────────────────
        System.out.println("\n--- 9. Time Complexity Analysis ---");
        System.out.println("  Insert            : O(log n)  — splits propagate up at most h levels");
        System.out.println("  Search            : O(log n)  — traverse from root to leaf");
        System.out.println("  Range Search      : O(log n + k)  — k = matching records");
        System.out.println("  Sequential Scan   : O(n)  — follow leaf linked list");
        System.out.println("  Discharge/Update  : O(log n)  — search + in-place update");
        System.out.println("  Space Complexity  : O(n)");
        System.out.println("  BST Search        : O(log n) avg but O(n) worst (unbalanced)");
        System.out.println("  => B+ Tree guarantees O(log n) always + O(n) sequential scan");
        System.out.println("     making it ideal for large-scale hospital databases.");

        System.out.println("\n============================================================");
        System.out.println("  Process finished with exit code 0");
        System.out.println("============================================================");
    }
}
