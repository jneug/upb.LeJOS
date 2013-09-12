
package de.upb.ddi.lejos.comm;


import de.upb.ddi.lejos.util.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.RemoteDevice;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;


/**
 * Bluetooth Verbindung zwischen NXT Bausteinen. Stellt Verbindungen
 * zwischen Bausteinen her und Methoden zum Versandt von Daten bereit.
 * Für weitere Informationen siehe {@link RemoteConnection}.
 *
 * @author Jonas Neugebauer <jneug@mail.upb.de>
 */
// TODO: Abstract to all device connections
public class BluetoothConnection implements RemoteConnection {

    /**
     * Timeout für Inquire-Vorgänge in 1.27 Sekunden
     * (<code>INQUIRE_TIMEOUT * 1.27</code>).
     */
    private static final int INQUIRE_TIMEOUT = 5;
    
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
     * Bluetooth-Adresse des Bausteins.
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

    /**
     * Konstruktor mit automatischer Bestimmung der Bluetooth-Adresse.
     * Die Aufrufe von {@link Bluetooth#getFriendlyName()} und
     * {@link Bluetooth#getLocalAddress()} können teils einige Sekunden in
     * Anspruch nehmen. Der alternative Konstruktor
     * {@link BluetoothConnection(String, String)} kann dies beschleunigen.
     * <p/>
     * Die erste Instanz der Klasse sollte ohne Parameter erstellt werden. Alle
     * weiteren mit {@link clone()} aus der ersten erzeugt werden.
     */
    public BluetoothConnection() {
        this.deviceName = Bluetooth.getFriendlyName();
        this.deviceAddress = Bluetooth.getAddress();
    }

    /**
     * Alternativer Konstruktor mit manueller Zuweisung der Adresse.
     * Im Gegensatz zum Standard-Konstruktor nimmt dieser Werte für
     * Bluetooth-Name und -Adresse entgegen. Dies kann den Startvorgang
     * deutlich beschleunigen, schränkt aber die Flexibilität des Programms
     * ein auf verschiedenen NXTs zu laufen, denn Name und Adresse müssen mit
     * denen des NXTs übereinstimmen.
     * <p/>
     * Wird von {@link clone()} benutzt.
     *
     * @param name
     * @param address
     */
    public BluetoothConnection( String name, String address ) {
        this.deviceName = name;
        this.deviceAddress = address;
    }

    /**
     * Gibt den Bluetooth Gerätenamen des NXTs zurück
     *
     * @return
     */
    public String getName() {
        return this.deviceName;
    }

    /**
     * Gibt dei Bluetooth Adresse des NXTs zurück
     *
     * @return
     */
    public String getAddress() {
        return this.deviceAddress;
    }

    /**
     * Gibt die Bluetooth Adresse des verbundenen Geräts zurück
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

    public void connect() {
        this.connect(CONNECT_TIMEOUT);
    }

    public void connect( int timeout ) {
        this.connectionAttempts++;

        Console.dbg.println("bt: waiting ...");
        this.connection = Bluetooth.waitForConnection(timeout, NXTConnection.PACKET);

        if( this.connection == null ) {
            Console.dbg.println("bt: no conn req");
            // TODO: Sollte hier eine Exception geworfen werden?
            // throw new CommException(CommException.TIMEOUT);
        } else {
            this.isConnected = true;

            Console.dbg.println("bt: connected to");
            Console.dbg.println("  " + this.getRemoteAddress());

            this.in = this.connection.openDataInputStream();
            this.out = this.connection.openDataOutputStream();
        }
    }

    public void connect( String identifier ) {
        this.connectionAttempts++;

        Console.dbg.println("bt: lookup knwn dev");
        RemoteDevice device = this.getKnownDevice(identifier);

        if( device == null ) {
            Console.dbg.println("bt: inquire ...");
            device = this.inquire(identifier);
        }

        if( device == null ) {
            Console.dbg.println("bt: dev not found");
            // TODO: Sollte hier eine Exception geworfen werden?
            // throw new CommException(CommException.CONN);
        } else {
            Console.dbg.println("bt: connecting ...");

            this.connection = Bluetooth.connect(device);
            if( this.connection == null ) {
                Console.dbg.println("bt: conn failed");
                // TODO: Sollte hier eine Exception geworfen werden?
                // throw new CommException(CommException.TIMEOUT);
            } else {
                this.isConnected = true;

                Console.dbg.println("bt: connected to");
                Console.dbg.println("  " + this.getRemoteAddress());

                this.in = this.connection.openDataInputStream();
                this.out = this.connection.openDataOutputStream();
            }
        }
    }

    /**
     * Ein bekanntes Gerät aus der Liste suchen.
     * <p/>
     * Die Methode sucht anhand eines Identifizierungs-Strings in der Liste
     * bekannter Geräte (also Geräte die zuvor gepaired wurden) nach einem
     * passenden Eintrag. Die Identifizierung kann eine Bluetooth-Adresse
     * oder ein Gerätename sein.<p/>
     * Innerhalb des Programmcodes sollte auf die Benutzung von Gerätenamen
     * verzichtet werden, da die Adressen eine eindeutigere Identifizierung
     * erlauben.
     *
     * @param identifier Name oder Adresse des anderen Geräts
     * @return Das Gerät als <code>RemoteDevice</code>, falls es gefunden wurde.
     *  Sonst <code>NULL</code>.
     */
    protected RemoteDevice getKnownDevice( String identifier ) {
        boolean inquire_address = Bluetooth.isAddress(identifier);

        Vector<RemoteDevice> knownDevices = Bluetooth.getKnownDevicesList();
        for( int i = 0; i < knownDevices.size(); i++ ) {
            RemoteDevice btrd = knownDevices.elementAt(i);

            if( inquire_address ) {
                if( btrd.getBluetoothAddress().equals(identifier) ) {
                    return btrd;
                }
            } else {
                if( btrd.getFriendlyName(false).equals(identifier) ) {
                    return btrd;
                }
            }
        }

        // Kein passendes Gerät gefunden
        return null;
    }

    /**
     * Der Inquire-Prozess versucht unbekannte Bluetooth Geräte zu finden und 
     * zu pairen (der Liste der bekannten Geräte hinzuzufügen).
     * @todo Findet momentan nur andere NXTs und keine PCs und Smartphones. (-> Anpassen der cod)
     * @param identifier Name oder Adresse des anderen Geräts
     * @return Das Gerät als <code>RemoteDevice</code>, falls es gefunden wurde.
     *  Sonst <code>NULL</code>.
     */
    protected RemoteDevice inquire( String identifier ) {
        boolean inquire_address = Bluetooth.isAddress(identifier);

        byte[] cod = {0, 0, 8, 4}; // Geräteklasse der NXT-Steine
        //Vector inquireDevices = Bluetooth.inquire(10, INQUIRE_TIMEOUT, cod);
        Vector<RemoteDevice> inquireDevices = Bluetooth.inquire(10, INQUIRE_TIMEOUT, 84);
        for( int i = 0; i < inquireDevices.size(); i++ ) {
            RemoteDevice btrd = inquireDevices.elementAt(i);

            if( inquire_address ) {
                if( btrd.getBluetoothAddress().equals(identifier) ) {
                    Bluetooth.addDevice(btrd);
                    return btrd;
                }
            } else {
                if( btrd.getFriendlyName(false).equals(identifier) ) {
                    Bluetooth.addDevice(btrd);
                    return btrd;
                }
            }
        }

        // Kein passendes Gerät gefunden
        return null;
    }

    public void sendByte( byte value ) throws CommException {
        if( !this.isConnected() ) {
            throw new CommException();
        }

        try {
            this.out.writeByte(value);
            this.out.flush();
        } catch( IOException e ) {
            Console.err.println("bt: send failed");
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
            Console.err.println("bt: send failed");
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
            Console.err.println("bt: send failed");
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
            Console.err.println("bt: send failed");
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
            Console.err.println("bt: recv failed");
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
            Console.err.println("bt: recv failed");
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
            Console.err.println("bt: recv failed");
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
            Console.err.println("bt: recv failed");
            throw new CommException("Failed to receive string", CommException.RECV);
        }

        return new String(str);
    }

    @Override
    public RemoteConnection clone() {
        return new BluetoothConnection(this.deviceName, this.deviceAddress);
    }

}
