package de.upb.ddi.lejos.util;


/**
 * Ein synchroner Zeitmesser. Im Gegensatz zum {@link lejos.util.Timer}, der in
 * einem eigenen {@code Thread} läuft, muss der {@code SynchronousTimer} von der
 * Elternklasse in regelmäßigen Abständen explizit aufgerufen werden. Die Klasse
 * bietet sich an, um festzustellen, ob ein festgelegter Zeitraum
 * <em>mindestens</em> vergangen ist. Um zu einem bestimmten Zeitpunkt eine
 * Aktion auszulösen, sollte der {@code Timer} und ein
 * {@link lejos.util.TimerListener} genutzt werden.
 * <p/>
 * <pre>
 * // 5 Sekunden Timer erstellen 
 * SynchronousTimer t = new SynchronousTimer(5000);
 * while( !t.countdownFinished() ) {
 * 	System.out.println("Der Timer läuft seit "+t.timeElapsed()+" Millisekunden");
 * 	System.out.println("Noch "+t.timeRemaining()+" Millisekunden verbleiben");
 * 
 * 	{@link lejos.util.Delay Delay}.delay(100);
 * }
 * </pre>
 * 
 * @author Jonas Neugebauer <jneug@mail.upb.de>
 */
public class SynchronousTimer {

	/**
	 * Startzeitpunkt des Countdowns
	 */
	private long start;

	/**
	 * Zielzeitpunkt des Countdowns
	 */
	private long target;

	/**
	 * Länge des Countdown
	 */
	private long countdown = 0;

	/**
	 * Initialisierung des Timers
	 */
	public SynchronousTimer() {
		this.reset();
	}

	/**
	 * Initialisierung des Timers mit einem festen Countdown
	 * 
	 * @param cntdwn Countdown in Millisekunden
	 */
	public SynchronousTimer( long cntdwn ) {
		this.setCountdown(cntdwn);
		this.reset();
	}

	/**
	 * Setzt den Countdown zurück und startet ihn neu.
	 */
	public final void reset() {
		this.start = System.currentTimeMillis();
		this.target = this.start + this.countdown;
	}

	/**
	 * Setzt den Zeitraum des Countdown neu.
	 * 
	 * @param cnt Countdown in Millisekunden
	 */
	public final void setCountdown( long cnt ) {
		this.countdown = cnt;
	}

	/**
	 * @return Der Zeitraum des Countdown in Millisekunden
	 */
	public long getCountdown() {
		return this.countdown;
	}

	/**
	 * Gibt die vergangene Zeit in Millisekunden seit dem letzten {@link #reset()} zurück.
	 * 
	 * @return
	 */
	public int timeElapsed() {
		return (int) (System.currentTimeMillis() - this.start);
	}

	/**
	 * Gibt die verbleibende Zeit in Millisekunden des Countdowns zurück oder Null wenn der
	 * Countdown abgelaufen ist.
	 * 
	 * @return
	 */
	public int timeRemaining() {
		return (int) Math.max(0, this.target - System.currentTimeMillis());
	}

	/**
	 * @return {@code true} wenn der Zeitraums des Countdowns abgelaufen ist.
	 */
	public boolean countdownFinished() {
		return (this.timeRemaining() == 0);
	}

}