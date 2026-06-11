package datastoragemessaging;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataStorageMessaging {

   
    static ArrayList<Message> sentMessages = new ArrayList<>();
    static ArrayList<Message> disregardedMessages = new ArrayList<>();
    static ArrayList<Message> storedMessages = new ArrayList<>();
    static ArrayList<String> messageHashes = new ArrayList<>();
    static ArrayList<String> messageIDs = new ArrayList<>();

    static final String FILE_NAME = "messages.json";

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        
        System.out.print("Enter your first name: ");
        String firstName = input.nextLine();
        System.out.print("Enter your last name: ");
        String lastName = input.nextLine();
        
        String userName = "";
        boolean validUsername = false;
        while (!validUsername) {
            System.out.print("Enter Your Username: ");
            userName = input.nextLine();
            validUsername = userName.contains("_") && userName.length() <= 5;
            if (validUsername) System.out.println("Username Successfully created");
            else System.out.println("Username must contain '_' and be <= 5 characters. Try again.");
        }

        String password = "";
        boolean validPassword = false;
        while (!validPassword) {
            System.out.print("Enter Your Password: ");
            password = input.nextLine();
            boolean hasUpper = false, hasDigit = false, hasSpecial = false;
            for (char c : password.toCharArray()) {
                if (Character.isUpperCase(c)) hasUpper = true;
                else if (Character.isDigit(c)) hasDigit = true;
                else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
            }
            validPassword = (password.length() >= 8) && hasUpper && hasDigit && hasSpecial;
            if (validPassword) System.out.println("Password successfully created.");
            else System.out.println("Password should have 8+ characters, 1 capital, 1 number, 1 special character.");
        }

        String cellNumber = "";
        boolean validCell = false;
        while (!validCell) {
            System.out.print("Enter your cell phone number with country code: ");
            cellNumber = input.nextLine();
            validCell = cellNumber.startsWith("+") && cellNumber.length() >= 10 && cellNumber.length() <= 14;
            if (validCell) {
                for (int i = 1; i < cellNumber.length(); i++) {
                    if (!Character.isDigit(cellNumber.charAt(i))) { validCell = false; break; }
                }
            }
            if (validCell) System.out.println("Cell phone number successfully added.");
            else System.out.println("Cell phone number is not correctly formatted or is missing +code.");
        }

        System.out.println("\nRegistration complete. Please log in.\n");

        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter username to login: ");
            String loginUser = input.nextLine();
            System.out.print("Enter password to login: ");
            String loginPass = input.nextLine();
            if (loginUser.equals(userName) && loginPass.equals(password)) {
                System.out.println("Welcome " + firstName + ", " + lastName + " it is great to see you again.");
                loggedIn = true;
            } else System.out.println("Username or password incorrect, please try again.");
        }

        loadMessages();

        int choice = -1;
        System.out.println("Welcome to QuickChat.");
        do {
            System.out.println("\n1. Send Messages");
            System.out.println("2. Show recently sent messages");
            System.out.println("3. Quit");
            System.out.println("4. Stored Messages Report");
            System.out.print("Choose an option: ");

            String choiceStr = input.nextLine();
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a number 1-4");
                continue;
            }

            switch(choice) {
                case 1 -> sendMessages(input);
                case 2 -> displayMessages();
                case 3 -> {
                    saveMessages();
                    System.out.println("Leaving QuickChat. Goodbye!");
                }
                case 4 -> storedMessagesMenu(input);
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice!= 3);
    }

    static void storedMessagesMenu(Scanner input) {
        int opt = -1;
        do {
            System.out.println("\n--- Stored Messages Report ---");
            System.out.println("1. Display sender and recipient of all sent messages");
            System.out.println("2. Display longest sent message");
            System.out.println("3. Search for a message by ID");
            System.out.println("4. Search for all messages sent to a recipient");
            System.out.println("5. Delete a message using message hash");
            System.out.println("6. Display report");
            System.out.println("0. Back to main menu");
            System.out.print("Choose: ");

            String choiceStr = input.nextLine();
            try {
                opt = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a number 0-6");
                continue;
            }

            switch(opt) {
                case 1 -> displaySenderRecipient();
                case 2 -> displayLongestSent();
                case 3 -> {
                    System.out.print("Enter Message ID: ");
                    String id = input.nextLine();
                    Message m = searchByID(id);
                    System.out.println(m!= null? m.printMessages() : "Message ID not found.");
                }
                case 4 -> {
                    System.out.print("Enter recipient: ");
                    String rec = input.nextLine();
                    ArrayList<Message> list = searchByRecipient(rec);
                    if (list.isEmpty()) System.out.println("No messages for that recipient.");
                    else list.forEach(m -> System.out.println(m.printMessages() + "\n"));
                }
                case 5 -> {
                    System.out.print("Enter Message Hash: ");
                    String hash = input.nextLine();
                    boolean del = deleteByHash(hash);
                    if (!del) System.out.println("Hash not found.");
                }
                case 6 -> displayFullReport();
                case 0 -> System.out.println("Going back...");
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (opt!= 0);
    }

    static void displaySenderRecipient() {
        ArrayList<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        if (all.isEmpty()) { System.out.println("No messages."); return; }
        for (Message m : all) {
            System.out.println("Recipient: " + m.recipient + " | Message: " + m.message);
        }
    }

    static void displayLongestSent() {
        Message longest = getLongestMessage();
        if (longest!= null) System.out.println("Longest message:\n" + longest.printMessages());
        else System.out.println("No messages.");
    }

    static Message getLongestMessage() {
        ArrayList<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        if (all.isEmpty()) return null;
        Message longest = all.get(0);
        for (Message m : all) {
            if (m.message.length() > longest.message.length()) longest = m;
        }
        return longest;
    }

    static Message searchByID(String id) {
        for (Message m : sentMessages) if (m.messageID.equals(id)) return m;
        for (Message m : storedMessages) if (m.messageID.equals(id)) return m;
        return null;
    }

    static ArrayList<Message> searchByRecipient(String recipient) {
        ArrayList<Message> result = new ArrayList<>();
        for (Message m : sentMessages) if (m.recipient.equals(recipient)) result.add(m);
        for (Message m : storedMessages) if (m.recipient.equals(recipient)) result.add(m);
        return result;
    }

    static boolean deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).messageHash.equals(hash)) {
                Message removed = storedMessages.remove(i);
                messageHashes.remove(hash);
                messageIDs.remove(removed.messageID);
                System.out.println("Message \"" + removed.message + "\" successfully deleted.");
                return true;
            }
        }
        for (int i = 0; i < sentMessages.size(); i++) {
            if (sentMessages.get(i).messageHash.equals(hash)) {
                Message removed = sentMessages.remove(i);
                messageHashes.remove(hash);
                messageIDs.remove(removed.messageID);
                System.out.println("Message \"" + removed.message + "\" successfully deleted.");
                return true;
            }
        }
        return false;
    }

    static void displayFullReport() {
        System.out.println("\n--- FULL REPORT OF SENT MESSAGES ---");
        if (sentMessages.isEmpty()) { System.out.println("No sent messages."); return; }
        for (Message m : sentMessages) {
            System.out.println("Message Hash: " + m.messageHash);
            System.out.println("Recipient: " + m.recipient);
            System.out.println("Message: " + m.message);
            System.out.println("--------------------");
        }
    }

    static void sendMessages(Scanner input) {
        System.out.print("How many messages do you want to send? ");
        int num = 0;
        try {
            num = Integer.parseInt(input.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Returning to menu.");
            return;
        }

        for (int i = 0; i < num; i++) {
            String recipient;
            while (true) {
                System.out.print("Enter recipient with + and max 10 digits: ");
                recipient = input.nextLine();
                Message temp = new Message(recipient, "");
                String check = temp.checkRecipientCell();
                System.out.println(check);
                if (check.equals("Cell phone number successfully saved.")) break;
                System.out.println("Try again.\n");
            }

            String msg;
            Message m = null;
            while (true) {
                System.out.print("Enter message: ");
                msg = input.nextLine();
                m = new Message(recipient, msg);
                System.out.println(m.checkMessageLength());
                if (m.checkMessageLength().equals("Message ready to send.")) break;
                else System.out.println("Try again.\n");
            }

            m.createMessageHash();
            System.out.println("1. Send Message\n2. Disregard Message\n3. Store Message");

            int opt = 0;
            try {
                opt = Integer.parseInt(input.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid option. Message disregarded.");
                continue;
            }

            String result = m.sentMessage(opt);
            System.out.println(result);
            System.out.println("\n--- Message Details ---");
            System.out.println("ID: " + m.messageID);
            System.out.println("Hash: " + m.messageHash);
            System.out.println("Recipient: " + m.recipient);
            System.out.println("Message: " + m.message);
        }
    }

    static void displayMessages() {
        if (sentMessages.isEmpty()) { System.out.println("No messages sent yet."); return; }
        System.out.println("\n--- Recently Sent Messages ---");
        for (Message m : sentMessages) System.out.println(m.printMessages() + "\n");
    }

    static void saveMessages() {
        try {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < storedMessages.size(); i++) {
                Message m = storedMessages.get(i);
                json.append("{\"messageID\":\"").append(m.messageID).append("\",");
                json.append("\"recipient\":\"").append(m.recipient).append("\",");
                json.append("\"message\":\"").append(m.message.replace("\"", "\\\"")).append("\",");
                json.append("\"messageHash\":\"").append(m.messageHash).append("\"}");
                if (i < storedMessages.size() - 1) json.append(",");
            }
            json.append("]");
            Files.write(Paths.get(FILE_NAME), json.toString().getBytes());
            System.out.println("Messages saved to " + FILE_NAME);
        } catch (Exception e) { System.out.println("Error saving: " + e.getMessage()); }
    }

    static void loadMessages() {
        try {
            if (!Files.exists(Paths.get(FILE_NAME))) return;
            String content = new String(Files.readAllBytes(Paths.get(FILE_NAME))).trim();
            if (content.equals("[]")) return;
            content = content.substring(1, content.length() - 1);
            String[] objects = content.split("\\},\\{");
            for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "");
                String[] parts = obj.split("\",\"");
                String id = parts[0].split(":\"")[1].replace("\"", "");
                String recipient = parts[1].split(":\"")[1].replace("\"", "");
                String message = parts[2].split(":\"")[1].replace("\"", "").replace("\\\"", "\"");
                String hash = parts[3].split(":\"")[1].replace("\"", "");
                Message m = new Message(recipient, message);
                m.messageID = id;
                m.messageHash = hash;
                storedMessages.add(m);
                messageIDs.add(id);
                messageHashes.add(hash);
            }
            System.out.println("Loaded " + storedMessages.size() + " stored messages from JSON");
        } catch (Exception e) { System.out.println("No saved messages found."); }
    }
}

class Message {
    String messageID;
    String recipient;
    String message;
    String messageHash;

    public Message(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
        this.messageID = generateMessageID();
    }

    String generateMessageID() {
        Random rand = new Random();
        return String.valueOf(10000000L + rand.nextLong(90000000L));
    }

    String checkMessageLength() {
        return message.length() <= 250? "Message is ready to send." : "Please enter a message of less than 250 characters.";
    }

    String checkRecipientCell() {
        return recipient.length() <= 11 && recipient.startsWith("+")?
               "Cell phone number successfully captured." :
               "Cell phone number is incorrectly formatted or does not contain an international code.";
    }

    void createMessageHash() {
        String[] words = message.trim().split("\\s+");
        String first = words.length > 0? words[0].toUpperCase() : "";
        String last = words.length > 1? words[words.length - 1].toUpperCase() : first;
        this.messageHash = messageID.substring(0, 2) + ":" + messageID.length() + ":" + first + last;
    }

    String sentMessage(int option) {
        switch (option) {
            case 1 -> {
                DataStorageMessaging.sentMessages.add(this);
                DataStorageMessaging.messageIDs.add(messageID);
                DataStorageMessaging.messageHashes.add(messageHash);
                return "Message successfully sent.";
            }
            case 2 -> {
                DataStorageMessaging.disregardedMessages.add(this);
                DataStorageMessaging.messageIDs.add(messageID);
                DataStorageMessaging.messageHashes.add(messageHash);
                return "Message disregarded.";
            }
            case 3 -> {
                DataStorageMessaging.storedMessages.add(this);
                DataStorageMessaging.messageIDs.add(messageID);
                DataStorageMessaging.messageHashes.add(messageHash);
                return "Message successfully stored.";
            }
            default -> { return "Invalid option."; }
        }
    }

    public boolean checkMessageID() { return messageID.length() == 10; }

    public String printMessages() {
        return "ID: " + messageID + "\nHash: " + messageHash +
               "\nRecipient: " + recipient + "\nMessage: " + message;
    }
}