package com.softawii;

import com.softawii.curupira.core.Curupira;

import javax.security.auth.login.LoginException;

public class Main {
    public static void main(String[] args) throws LoginException, InterruptedException {
        Curupira curupira = new Curupira("com.softawii.example", null);
    }
}