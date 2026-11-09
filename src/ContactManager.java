import java.util.ArrayList;
import java.util.List;

public class ContactManager {
    private List<Contact> contacts;

    public ContactManager() {
        contacts = new ArrayList<>();
    }

    public List<Contact> getAllContacts() {
        return contacts;
    }

    public void addContact(Contact c) throws IllegalArgumentException {
        if (c.getName().trim().isEmpty() || c.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Name/Phone cannot be empty!");
        }
        if (!c.getPhoneNumber().matches("\\d+")) {
            throw new IllegalArgumentException("Phone must contain only digits!");
        }
        if (contacts.stream().anyMatch(contact -> contact.getPhoneNumber().equals(c.getPhoneNumber()))) {
            throw new IllegalArgumentException("Phone number already exists!");
        }
        contacts.add(c);
    }

    public void editContact(String oldPhone, String newName, String newPhone) {
        for (Contact c : contacts) {
            if (c.getPhoneNumber().equals(oldPhone)) {
                c.setName(newName);
                c.setPhoneNumber(newPhone);
            }
        }
    }

    public void deleteContact(String phone) {
        contacts.removeIf(c -> c.getPhoneNumber().equals(phone));
    }

    public void markFavorite(String phone, boolean isFav) {
        for (Contact c : contacts) {
            if (c.getPhoneNumber().equals(phone)) c.setFavorite(isFav);
        }
    }

    public void markEmergency(String phone, boolean isEm) {
        for (Contact c : contacts) {
            if (c.getPhoneNumber().equals(phone)) {
                c.setEmergency(isEm);
                break; // Important to break after finding the contact
            }
        }
    }

    public List<Contact> getFavoriteContacts() {
        List<Contact> favs = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.isFavorite()) favs.add(c);
        }
        return favs;
    }

    public List<Contact> getEmergencyContacts() {
        List<Contact> ems = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.isEmergency()) ems.add(c);
        }
        return ems;
    }
}