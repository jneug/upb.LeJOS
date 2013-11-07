package de.upb.ddi.lejos.util;


import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;

/**
 * Console um Informationen auf dem LCD auszugeben. Die Konsole stellt drei
 * Ausgabeströme zur Verfügung, die es ermöglichen, semantisch getrennt Daten
 * auf dem Display enzuzeigen. Die einzelnen Ströme können jederzeit ein- und
 * abgeschaltet werden. Auf diese Weise können zusätzliche Debug-Informationen
 * eingeschaltet, Log-Daten angezeigt oder nur die vom Programm generierten
 * Ausgaben auf dem LCD angezeigt werden. Ausserdem kann die Ausgabe durch einen
 * Hinweiston ergänzt werden.
 * 
 * @author Jonas Neugebauer <jonas.neugebauer@upb.de>
 */
public class Console {

	/**
	 * Konsole zur normalen Ausgabe von Informationen auf dem Display.
	 * Bevorzugte Methode zu {@link System#out}. Ist zu Beginn aktiv.
	 */
	public static final Console out = new Console();

	/**
	 * Konsole zur Fehlerausgabe. Exceptionsund Programmfehler sollten über
	 * diese Ausgabe geschickt werden. Ist zu Beginn aktiv und erzeugt neben dem
	 * Nachrichtentext einen Fehlerton.
	 */
	public static final Console err = new Console(true, Console.SOUND_BUZZ);

	/**
	 * Konsole zur Ausgabe von Log-Nachrichten zum Programmablauf. Sollte für
	 * Statusnachrichten genutzt werden, die Informationen über den Betrieb
	 * geben. Ist zu Beginn inaktiv. Im aktiven Fall werden die Nachrichten von
	 * einem Hinweiston begleitet.
	 */
	public static final Console log = new Console(false, Console.SOUND_BEEP);

	/**
	 * Debugging-Konsole. Nachrichten über diese Ausgabe dienen der reinen
	 * Fehlersuche. Ausgaben von Variablen und Programmdetails sollten
	 * ausschließlich über diesen Ausgabestrom gehen. Ist zu Beginn inaktiv.
	 */
	public static final Console dbg = new Console(false);

	/**
	 * Leitet alle Ausgabeströme über die {@link RConsole} um.
	 * 
	 * @see Console#redirect()
	 */
	public static void redirectAll() {
		out.redirect();
		log.redirect();
		err.redirect();
		dbg.redirect();
	}

	/**
	 * Beendet die Umleitung über die {@link RConsole}. Sollte am Ende des
	 * Programmablaufs aufgerufen werden falls {@link Console#redirect()}
	 * genutzt wurde. Der über {@link ButtonUtils} eingerichtete Abbruch-Button
	 * beendet jede Umleitung automatisch.
	 */
	public static void close() {
		if( RConsole.isOpen() )
			RConsole.close();
	}

	// Konstanten für Hinweistöne
	/**
	 * Kein Hinweiston.
	 */
	public static final int SOUND_OFF = 0;
	/**
	 * {@link Sound#beep()} als Hinweiston.
	 */
	public static final int SOUND_BEEP = 1;
	/**
	 * {@link Sound#buzz()} als Hinweiston.
	 */
	public static final int SOUND_BUZZ = 2;
	/**
	 * {@link Sound#twoBeeps()} als Hinweiston.
	 */
	public static final int SOUND_TWOBEEP = 3;


	/**
	 * Status der Konsole.
	 */
	private boolean enabled = true;

	/**
	 * Ob die Ausgabe über die {@link RConsole} Klasse umgeleitet werden soll.
	 */
	private boolean redirect = false;

	/**
	 * Art des Hinweiston.
	 * 
	 * @see #SOUND_OFF
	 * @see #SOUND_BEEP
	 * @see #SOUND_BUZZ
	 * @see #SOUND_TWOBEEP
	 */
	private int sound = SOUND_OFF;

	/**
	 * Konstruktor
	 */
	private Console() {
	}

	/**
	 * Konstruktor
	 * 
	 * @param enabled Einstellung ob Konsole aktiv.
	 */
	private Console( boolean enabled ) {
		this.enabled = enabled;
	}

	/**
	 * Konstruktor
	 * 
	 * @param enabled Einstellung ob Konsole aktiv.
	 * @param sound Art des Hinweistons.
	 */
	private Console( boolean enabled, int sound ) {
		this.enabled = enabled;
		this.sound = sound;
	}

	/**
	 * Aktivierung der Konsole.
	 */
	public void enable() {
		this.enabled = true;
	}

	/**
	 * Deaktivierung der Konsole.
	 */
	public void disable() {
		this.enabled = false;
	}

	/**
	 * @return <code>true</code> wenn Konsole aktiv.
	 */
	public boolean enabled() {
		return this.enabled;
	}

	/**
	 * Hinweistöne abschalten.
	 */
	public void mute() {
		this.sound = SOUND_OFF;
	}

	/**
	 * Ausgabe eines Strings über die Konsole.
	 * 
	 * @param msg
	 */
	public void print( String msg ) {
		if( !this.enabled )
			return;

		if( this.redirectPossible() )
			RConsole.print(msg);
		else
			System.out.print(msg);
	}

	/**
	 * Ausgabe eines String über die Konsole mit einem Zeilenumbruch am Ende.
	 * 
	 * @param msg
	 */
	public void println( String msg ) {
		if( !this.enabled )
			return;
		this.signal();

		if( this.redirectPossible() )
			RConsole.println(msg);
		else
			System.out.println(msg);
	}

	/**
	 * Hinweiston abspielen
	 */
	public void signal() {
		switch( this.sound ) {
			case SOUND_BEEP:
				Sound.beep();
				break;
			case SOUND_BUZZ:
				Sound.buzz();
				break;
			case SOUND_TWOBEEP:
				Sound.twoBeeps();
				break;
		}
	}

	/**
	 * Ausgabe über die {@link RConsole} umleiten. Bei umfangreichen Ausgaben
	 * reicht das Display des NXTs oftmals nicht aus, daher lassen sich
	 * beliebige der Konsolen auch auf einem über USB angeschlossenen
	 * Ausgabegerät anzeigen. Dies ist vor allem für die {@link #dbg
	 * Debug-konsole} hilfreich, da diese in der Regel eine große Anzahl
	 * Informationen erzeugt, die zum Großteil eher unübersichtlich sind.
	 * <p/>
	 * Als Ausgabegerät bietet sich ein Computer an, auf dem das von LeJOS
	 * mitgelieferte Programm <code>nxjconsole</code> bzw
	 * <code>nxjconsoleviewer</code> läuft (zu finden im <em>bin</em> Ordner der
	 * LeJOS Installation).
	 */
	public void redirect() {
		if( !RConsole.isOpen() )
			RConsole.open();
		this.redirect = RConsole.isOpen();
	}

	/**
	 * Gibt an ob die Umleitung der Konsole momentan aktiv ist. Dies ist der
	 * Fall wenn {@link redirect()} aufgerufen wurde und die Verbindung über USB
	 * akzeptiert wurde.
	 * 
	 * @see RConsole#isOpen()
	 * @return
	 */
	private boolean redirectPossible() {
		return (this.redirect && RConsole.isOpen());
	}

}
