
package de.upb.ddi.lejos.com;

/**
 *
 * @author Jonas Neugebauer <jneug@mail.upb.de>
 */
public class ComException extends Exception {

    public static final int CONN = 0, SEND = 1, RECV = 2, TIMEOUT = 3;

    private int type = CONN;

    /**
     * Creates a new instance of <code>CommException</code> without detail message.
     */
    public ComException() {
    }

    /**
     * Constructs an instance of <code>CommException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ComException(String msg) {
        super(msg);
    }

    public ComException(int type) {
        this.type = type;
    }

    public ComException(String msg, int type) {
        super(msg);

        this.type = type;
    }

    /*
     * @return Den Fehlercode der Kommunikations-Exception
     */
    public int getType() {
        return this.type;
    }
    
}
