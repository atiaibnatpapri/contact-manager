import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String BINARY_FILE = "contacts.dat";
    private static final String CSV_FILE = "contacts.csv";

    // Binary serialization
    public static void saveContacts(List<Contact> contacts) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BINARY_FILE))) {
            oos.writeObject(contacts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Contact> loadContacts() {
        File file = new File(BINARY_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BINARY_FILE))) {
            return (List<Contact>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // CSV handling
    public static void exportToCSV(List<Contact> contacts) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println("Name,Phone,Favorite,Emergency");
            for (Contact c : contacts) {
                writer.printf("%s,%s,%b,%b%n",
                        c.getName(), c.getPhoneNumber(), c.isFavorite(), c.isEmergency());
            }
        }
    }

    public static List<Contact> importFromCSV() throws IOException {
        List<Contact> contacts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                Contact c = new Contact(parts[0], parts[1]);
                c.setFavorite(Boolean.parseBoolean(parts[2]));
                c.setEmergency(Boolean.parseBoolean(parts[3]));
                contacts.add(c);
            }
        }
        return contacts;
    }
}