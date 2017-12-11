package hu.ait.android.aitlastchances.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by emmakennelly on 12/9/17.
 */

public class User {


    private String username;

    private List<String> connections;
    private List<String> matches;


    public User(String username) {
        this.username = username;
        this.connections = new ArrayList<String>();
        this.matches = new ArrayList<String>();
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getConnections() {
        return connections;
    }

    public void setConnections(List<String> connections) {
        this.connections = connections;
    }

    public List<String> getMatches() {
        return matches;
    }

    public void setMatches(List<String> matches) {
        this.matches = matches;
    }

    public void addNewMatch(String match) {
        if (!this.matches.contains(match)) {
            this.matches.add(match);
        }
    }

    public void addNewConnection(String connection) {
        if (!this.connections.contains(connection)) {
            this.connections.add(connection);
        }
    }



}
