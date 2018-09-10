package com.donatien.customaccesspoint.AccessPoint;

/**
 * Enum for representing status of connected clients.
 */
public enum ClientStatus {
    APPROVED(1),
    DANGEROUS(-1),
    UNKNOWN(0);

    private int statusInt;

    ClientStatus(int statusInt) {
        this.statusInt = statusInt;
    }

    /**
     * Converts ClientStatus to integer.
     * Used to perform switch on status.
     * @return -1:DANGEROUS ; 0:UNKNOWN ; 1:APPROVED
     */
    public int toInteger() {
        return statusInt;
    }

    /**
     * Gets ClientStatus status from int status.
     * @param statusInt the int status
     * @return the ClientStatus status
     */
    public static ClientStatus getStatus(int statusInt) {
        switch(statusInt) {
            case -1:
                return ClientStatus.DANGEROUS;
            case 0:
                return ClientStatus.UNKNOWN;
            case 1:
                return ClientStatus.APPROVED;
            default:
                return ClientStatus.UNKNOWN;
        }
    }
}
