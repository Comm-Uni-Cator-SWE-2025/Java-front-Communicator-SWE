package com.swe.controller;

/**
 * A simple data class to hold client information (hostname and port).
 * This is the Java equivalent of a tuple(string, port).
 * @param hostName The IP address.
 * @param port The PORT.
 */
public record ClientNode(String hostName, int port) {
    @Override
    public int hashCode() {
        return (hostName + port).hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ClientNode other = (ClientNode) obj;
        if (port != other.port) {
            return false;
        }
        if (hostName == null) {
            return other.hostName == null;
        }
        return hostName.equals(other.hostName);
    }
}

