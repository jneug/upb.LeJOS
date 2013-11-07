package de.upb.ddi.lejos.util;


import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.util.Delay;

/**
 * Hilfsklasse zum Einrichten von Abbruch Buttons.
 * 
 * @author Zoe Werth <zwerth@it.cargotech.com>
 */
public class ButtonUtils {

	/**
	 * Privater Konstruktor.
	 */
	private ButtonUtils() {
	}

	/**
	 * Einrichten eines Abbruch-Buttons. Ein Beenden des Programms erfolgt
	 * nachdem der {@link Button#ESCAPE Escape-Button} eine Sekunde gedrückt
	 * gehalten wurde.
	 */
	public static void addExitListener() {
		addExitListener(Button.ESCAPE, 1000);
	}

	/**
	 * Einrichten eines Abbruch-Buttons. Ein Beenden des Programms erfolgt
	 * nachdem der gewählte Button eine Sekunde gedrückt gehalten wurde.
	 * 
	 * @param btn Button der zum Abbruch dienen soll.
	 */
	public static void addExitListener( Button btn ) {
		addExitListener(btn, 1000);
	}

	/**
	 * Einrichten eines Abbruch-Buttons. Ein Beenden des Programms erfolgt
	 * nachdem der {@link Button#ESCAPE Escape-Button} die festgelegte Zeit
	 * gedrückt gehalten wurde.
	 * 
	 * @param timeout Zeit bis beenden in Millisekunden.
	 */
	public static void addExitListener( int timeout ) {
		addExitListener(Button.ESCAPE, timeout);
	}

	/**
	 * Einrichten eines Abbruch-Buttons. Ein Beenden des Programms erfolgt
	 * nachdem der gewählte Button die festgelegte Zeit gedrückt gehalten wurde.
	 * 
	 * @param btn Button der zum Abbruch dienen soll.
	 * @param t Zeit bis beenden in Millisekunden.
	 */
	public static void addExitListener( Button btn, final int t ) {
		// Einrichten des Buttons über einen anonymen ButtonListener
		btn.addButtonListener(new ButtonListener() {
			/**
			 * Timeout bis beenden des Programms
			 */
			private int timeout = t;

			public void buttonPressed( Button b ) {
				// Falls ein Timeout eingestellt wurde wird nach Ablauf der
				// Button erneut geprüft und dann ggf beendet.
				if( timeout > 0 ) {
					Delay.msDelay(timeout);
					if( b.isDown() ) {
						this.exit();
					}
				}
			}

			public void buttonReleased( Button b ) {
				// Falls kein Timeout eingestellt wurde wird beendet, sobald der
				// Button losgelassen wird.
				if( timeout == 0 ) {
					this.exit();
				}
			}

			/**
			 * Beendet das Programm.
			 */
			private void exit() {
				System.exit(1);
			}
		});
	}

}
