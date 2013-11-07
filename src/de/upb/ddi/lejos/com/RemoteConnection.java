
package de.upb.ddi.lejos.com;

/**
 * Schnittstelle für Verbindungen. Im Normalfall wird die
 * Verbindung zwischen zwei NXTs über Bluetooth hergestellt. Es können aber 
 * auch USB Verbindungen zu externen Geräten implementiert werden.
 * <p/>
 * Implementierende Klassen stellen die unterste Schicht der Kommunikation
 * dar. Sie setzen direkt auf den entsprechenden Klassen
 * der jeweiligen Kommunikationsart (Bluetooth, USB) auf.
 * Die Klasse kennt sich nicht mit dem genutzten Protokoll innerhalb der
 * Kommunikation aus, sondern schickt nur unbearbietete Daten.
 * 
 * @author Jonas Neugebauer <jneug@mail.upb.de>
 */
public interface RemoteConnection {

    /**
     * Timeout für Verbindungsversuche in Millisekunden
     */
    public static final int CONNECT_TIMEOUT = 10000;

    /**
     * Neues Verbindungs-Objekt erzeugen. Dies ist die Bevorzugte Methode neue
     * Verbindungen auf einem NXT zu erzeugen, da sie die Verbindungs-Daten des
     * aktuellen Objekts übernimmt.
     * @return
     */
    public RemoteConnection clone();

    /**
     * Auf die Verbindung eines anderen Geräts warten. Es wird der
     * {@link #CONNECT_TIMEOUT} als Timeout benutzt.
     *
     * @see connect(int)
     */
    public void connect();

    /**
     * Auf die Verbindung eines anderen Geräts warten.
     *
     * @param timeout Wartezeit nach der abgebrochen wird in ms
     */
    public void connect( int timeout );

    /**
     * Verbindung zu einem anderen Gerät aufbauen.
     *
     * @param identifier Name oder Adresse des anderen Geräts
     */
    public void connect( String identifier );

    /**
     * Trennt eine bestehende Verbindung.
     */
    public void disconnect();

    /**
     * Gibt die Adresse des Geräts zurück.
     * @return
     */
    public String getAddress();

    /**
     * Gibt die Anzahl der bisherigen Verbindungsversuche zurück. Es werden alle
     * Versuche gezählt. Egal ob erfolgreich oder nicht.
     *
     * @return
     */
    public int getConnectionAttempts();

    /**
     * Gibt den Gerätenamen zurück.
     * @return
     */
    public String getName();

    /**
     * Gibt die Adresse des verbundenen Geräts zurück.
     * @return Die Adresse oder <code>null</code> falls keine Verbindung besteht
     */
    public String getRemoteAddress();

    /**
     * Prüft ob ein Verbindung hergestellt wurde.
     *
     * Die Methode stellt nicht fest ob die Verbindung zu einem NXT innerhalb
     * des Containerterminals besteht oder zu einem anderen Gerät. Dies ist
     * die Aufgabe der höher Stufigen Kommunikations Klassen.
     *
     * @return
     */
    public boolean isConnected();

    /**
     * Empfangen eines Bytes.
     * @return Das nächste empfangene Byte
     * @throws CommException Falls ein Verbindungsfehler auftritt
     */
    public byte receiveByte() throws CommException;

    /**
     * Empfangen eines Integers.
     * @return Der nächste empfangene Integer
     * @throws CommException Falls ein Verbindungsfehler auftritt
     */
    public int receiveInt() throws CommException;

    /**
     * Empfangen eines Long-Wertes.
     * @return Der nächste empfangenen Long
     * @throws CommException Falls ein Verbindungsfehler auftritt
     */
    public long receiveLong() throws CommException;

    /**
     * Empfangen eines Strings der mit {@link sendString(String)} gesendet 
     * wurde.
     * @return Der nächste empfangene String
     * @throws CommException Falls ein Verbindungsfehler auftritt
     */
    public String receiveString() throws CommException;

    /**
     * Setzt den Zähler für Verbindungsversuche zurück.
     */
    public void resetConnectionAttempts();

    /**
     * Senden eines Bytes.
     * @param value
     * @throws CommException Falls ein Verbindungsfehler auftritt
     */
    public void sendByte( byte value ) throws CommException;

    /**
     * Senden eines Integers.
     * @param value
     * @throws CommException Falls ein Verbindungsfehler auftritt
     */
    public void sendInt( int value ) throws CommException;

    /**
     * Senden eines Long-Wertes.
     * @param value
     * @throws CommException Falls ein Verbindungsfehler auftritt
     */
    public void sendLong( long value ) throws CommException;

    /**
     * Senden eines Strings.
     * @param value
     * @throws CommException Falls ein Verbindungsfehler auftritt
     */
    public void sendString( String value ) throws CommException;
    
}
