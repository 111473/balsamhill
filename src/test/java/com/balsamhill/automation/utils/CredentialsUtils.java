package com.balsamhill.automation.utils;

public class CredentialsUtils {

    /**
     * Retrieves the username from the environment variable BH_USERNAME.
     *
     * @return The username as a String.
     * @throws RuntimeException if the environment variable is not set or is empty.
     */
    public static String getUsername() {
        String username = System.getenv("BH_USERNAME");
        username.trim();
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("Environment variable BH_USERNAME is not set or empty");
        }
        return username;
    }

    /**
     * Retrieves the password from the environment variable BH_PASSWORD.
     *
     * @return The password as a String.
     * @throws RuntimeException if the environment variable is not set or is empty.
     */
    public static String getPassword() {
        String password = System.getenv("BH_PASSWORD");
        password.trim();
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("Environment variable BH_PASSWORD is not set or empty");
        }
        return password;
    }
}
