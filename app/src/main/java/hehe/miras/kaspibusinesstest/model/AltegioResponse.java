package hehe.miras.kaspibusinesstest.model;

import java.util.List;

public class AltegioResponse {
    private boolean success;
    private List<Appointment> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Appointment> getData() {
        return data;
    }
}
