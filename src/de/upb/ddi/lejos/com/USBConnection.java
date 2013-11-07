
package de.upb.ddi.lejos.com;

import de.upb.ddi.lejos.util.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;



/**
 *
 * @author Jonas Neugebauer <jneug@mail.upb.de>
 */
public class USBConnection implements RemoteConnection {
    
    /**
     * Timeout beim disconnect damit die Streams erfolgreich geschlossen werden in
     * Millisekunden.
     */
    private static final int DISCONNECT_TIMEOUT = 80;
    

    /**
     * Name des NXT Bausteins.
     */
    protected String deviceName;

    /**
     * USB-Adresse des Bausteins.
     */
    protected String deviceAddress;
    
    /**
     * Referenz zum Verbindungsobjekt
     */
    protected NXTConnection connection;

    /**
     * Eingabestrom (Empfang von Daten)
     */
    protected DataInputStream in;

    /**
     * Ausgabestrom (Versand von Daten)
     */
    protected DataOutputStream out;

    /**
     * Ob momentan eine Verbindung besteht
     */
    protected boolean isConnected = false;

    /**
     * Anzahl durchgeführter Verbindungsversuche (seit dem letzten Aufruf von
     * {@link #resetConnectionAttempts()}).
     */
    protected int connectionAttempts = 0;
    
    public USBConnection() {
        this.deviceName = USB.getName();
        this.deviceAddress = USB.getAddress();
    }
    
    public USBConnection( String name, String addr ) {
        this.deviceName = name;
        this.deviceAddress = addr;
    }
    
    /**
     * Gibt den USB Gerätenamen des NXTs zurück
     *
     * @return
     */
    public String getName() {
        return this.deviceName;
    }

    /**
     * Gibt dei USB Adresse des NXTs zurück
     *
     * @return
     */
    public String getAddress() {
        return this.deviceAddress;
    }

    /**
     * Gibt die USB Adresse des verbundenen Geräts zurück
     *
     * @return Die Adresse oder <code>null</code> falls keine Verbindung besteht
     */
    public String getRemoteAddress() {
        if( this.isConnected() ) {
            return this.connection.getAddress();
        } else {
            return null;
        }
    }

    public int getConnectionAttempts() {
        return this.connectionAttempts;
    }

    public void resetConnectionAttempts() {
        this.connectionAttempts = 0;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void connect() {
        this.connect(CONNECT_TIMEOUT);
    }

    public void connect( int timeout ) {
        this.connectionAttempts++;

        Console.dbg.println("usb: waiting ...");
        this.connection = USB.waitForConnection(timeout, NXTConnection.PACKET);
        
        if( this.connection == null ) {
            Console.dbg.println("usb: no conn req");
            // TODO: Sollte hier eine Exception geworfen werden?
            // throw new CommException(CommException.TIMEOUT);
        } else {
            this.isConnected = true;

            Console.dbg.println("usb: connected to");
            Console.dbg.println("  " + this.getRemoteAddress());

            this.in = this.connection.openDataInputStream();
            this.out = this.connection.openDataOutputStream();
        }
    }

    public void connect( String identifier ) {
        this.connectionAttempts++;

        Console.dbg.println("usb: connecting ...");
        this.connection = USB.getConnector()
                .connect(identifier, NXTConnection.PACKET);
        
        if( this.connection == null ) {
            Console.dbg.println("usb: conn failed");
            // TODO: Sollte hier eine Exception geworfen werden?
            // throw new CommException(CommException.TIMEOUT);
        } else {
            this.isConnected = true;

            Console.dbg.println("usb: connected to");
            Console.dbg.println("  " + this.getRemoteAddress());

            this.in = this.connection.openDataInputStream();
            this.out = this.connection.openDataOutputStream();
        }
    }

    public void disconnect() {
        if( this.isConnected() ) {
            try {
                this.in.close();
                this.out.close();

                Thread.sleep(DISCONNECT_TIMEOUT);
            } catch( IOException ex ) {
                //Exceptions.dispose(ex);
            } catch( InterruptedException ex ) { 
                /* Interrupt ist nicht schlimm ... */
            }
            this.connection.close();

            this.connection = null;
            this.in = null;
            this.out = null;

            this.isConnected = false;
        }
    }

    public void sendByte( byte value ) throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        try {
            this.out.writeByte(value);
            this.out.flush();
        } catch( IOException e ) {
            Console.err.println("usb: send failed");
            throw new CommException("Failed to send byte", CommException.SEND);
        }
    }

    public void sendInt( int value ) throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        try {
            this.out.writeInt(value);
            this.out.flush();
        } catch( IOException e ) {
            Console.err.println("usb: send failed");
            throw new CommException("Failed to send int", CommException.SEND);
        }
    }

    public void sendLong( long value ) throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        try {
            this.out.writeLong(value);
            this.out.flush();
        } catch( IOException e ) {
            Console.err.println("usb: send failed");
            throw new CommException("Failed to send long", CommException.SEND);
        }
    }

    public void sendString( String value ) throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        try {
            this.out.writeInt(value.length()); // Länge des String senden
            this.out.writeChars(value);
            this.out.flush();
        } catch( IOException e ) {
            Console.err.println("usb: send failed");
            throw new CommException("Failed to send string", CommException.SEND);
        }
    }

    public byte receiveByte() throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        byte value = -1;
        try {
            value = this.in.readByte();
        } catch( IOException e ) {
            Console.err.println("usb: recv failed");
            throw new CommException("Failed to receive byte", CommException.RECV);
        }

        return value;
    }

    public int receiveInt() throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        int value = -1;
        try {
            value = this.in.readInt();
        } catch( IOException e ) {
            Console.err.println("usb: recv failed");
            throw new CommException("Failed to receive int", CommException.RECV);
        }

        return value;
    }

    public long receiveLong() throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        long value = -1;
        try {
            value = this.in.readLong();
        } catch( IOException e ) {
            Console.err.println("usb: recv failed");
            throw new CommException("Failed to receive long", CommException.RECV);
        }

        return value;
    }

    public String receiveString() throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        char[] str = new char[]{0};
        try {
            int len = this.in.readInt(); // Länge des String empfangen

            str = new char[len];
            for( int i = 0; i < len; i++ ) {
                str[i] = this.in.readChar();
            }
        } catch( IOException e ) {
            Console.err.println("usb: recv failed");
            throw new CommException("Failed to receive string", CommException.RECV);
        }

        return new String(str);
    }
    
    @Override
    public RemoteConnection clone() {
        return new USBConnection(this.deviceName, this.deviceAddress);
    }
    
}
