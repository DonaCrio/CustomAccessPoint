package com.donatien.customaccesspoint.AccessPoint;

/**
 * Class used for representing a client connected to the wifi access point.
 */
public class Client {

    private String name;
    private String ipAddr;
    private String hwAddr;
    private ClientStatus status;

    /**
     * Constructor used to instantiate a known client.
     * @param name name of the client
     * @param ipAddr IP address of the client
     * @param hwAddr MAC address of the client
     * @param status {@link ClientStatus} of the client
     */
    public Client(String name, String ipAddr, String hwAddr, int status) {
        this.name = name;
        this.ipAddr = ipAddr;
        this.hwAddr = hwAddr;
        this.status = ClientStatus.getStatus(status);
    }

    /**
     * Constructor used to instantiate a new client.
     * @param ipAddr IP address of the client
     * @param hwAddr MAC address of the client
     */
    public Client(String ipAddr, String hwAddr) {
        this.name = "Unknown";
        this.ipAddr = ipAddr;
        this.hwAddr = hwAddr;
        this.status = ClientStatus.UNKNOWN;
    }

    /**
     * Constructor for FireBase.
     */
    public Client() {
        this.name = "Unknown";
        this.ipAddr = "0.0.0.0";
        this.hwAddr = "00:00:00:00:00:00";
        this.status = ClientStatus.UNKNOWN;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the IP address.
     * @return the IP address
     */
    public String getIpAddr() {
        return ipAddr;
    }

    /**
     * Gets the MAC address.
     * @return the MAC address
     */
    public String getHwAddr() {
        return hwAddr;
    }

    /**
     * Gets the status.
     * @return the status
     */
    public ClientStatus getStatus() {
        return status;
    }

    /**
     * Sets the name.
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the status.
     * @param status the status
     */
    public void setStatus(ClientStatus status) {
        this.status = status;
    }
}
