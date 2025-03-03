package hehe.miras.kaspibusinesstest.model;

import java.lang.reflect.Array;
import android.util.Log;

public class Appointment {
    private int id;
    private String date;
    private Client client;
    public String status = null;

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public Client getClient() {
        return client != null ? client : new Client();
    }

    public static class Client {
        private String phone;
        private String name;

        public String getPhone() {
            return phone;
        }

        public String getName()
        {
            return name;
        }
    }
}