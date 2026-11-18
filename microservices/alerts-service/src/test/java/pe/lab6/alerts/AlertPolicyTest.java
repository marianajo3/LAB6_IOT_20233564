package pe.lab6.alerts;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AlertPolicyTest {
 @Test void lowHumidityTriggersAlert(){assertTrue(AlertPolicy.evaluate(20,28).startsWith("Humedad baja"));}
 @Test void exactBoundariesAreNormal(){assertTrue(AlertPolicy.evaluate(30,35).startsWith("Condiciones"));}
 @Test void bothThresholdsAppear(){assertEquals("Humedad baja y temperatura alta",AlertPolicy.evaluate(20,36));}
}
