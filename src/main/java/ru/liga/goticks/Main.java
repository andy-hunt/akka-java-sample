package ru.liga.goticks;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class Main {

    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        String host = config.getString("http.host");
        int port = config.getInt("http.port");
        System.out.println(host + port);
    }
}
