package hehe.miras.kaspibusinesstest.model;

import java.lang.reflect.Array;
import android.util.Log;

public class Appointment {
    private int id;
    private String date;
    private Client client;
    private String status;
    private String phone;
    private String createdAt;

    public Appointment(int id, String date, Client client, String status, String phone, String createdAt) {
        this.id = id;
        this.date = date;
        this.client = client;
        this.status = status;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public Client getClient() {
        return client != null ? client : new Client();
    }

    public String getStatus() {
        return status;
    }

    public String getPhone() {
        return phone;
    }

    public String getCreatedAt() {
        return createdAt;
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