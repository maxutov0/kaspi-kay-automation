package hehe.miras.kaspibusinesstest.model;

public class AltegioAuthResponse {
    private boolean success;
    private AuthData data;

    public boolean isSuccess() {
        return success;
    }

    public AuthData getData() {
        return data;
    }

    public static class AuthData {
        private String user_token;

        public String getUserToken() {
            return user_token;
        }
    }
}