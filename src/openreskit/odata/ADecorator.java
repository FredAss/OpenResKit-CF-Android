package openreskit.odata;

/**
 * Decorator f�r die Anh�ngung einer Berechnung an eine Footprint
 * Position nach dem Decorator Pattern.
 * Siehe
 * http://www.philipphauer.de/study/se/design-pattern/decorator.php
 */
public abstract class ADecorator implements ICalculation {
	protected ICalculation iCalculation;
	String footprintPosition;
	public ADecorator(ICalculation calculation)
	{
		this.iCalculation = calculation;
	}
}  