import org.junit.Assert;
import org.junit.Test;

public class FirstTest {

    @Test
    public void testEquals() {
        String name = "murilo";
        Assert.assertEquals("murilo", name);
    }

    @Test
    public void testNotEqual() {
        String name = "murilo";
        Assert.assertNotEquals("Murilo", name);
    }
}
