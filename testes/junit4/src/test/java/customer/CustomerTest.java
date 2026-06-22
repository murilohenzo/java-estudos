package customer;

import com.murilo.domain.Customer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CustomerTest {

    @Test
    public void TestCustomerClass() {
        Customer customer = new Customer();

        customer.setName("Murilo");
        customer.setSex('M');

        Assert.assertEquals("Murilo", customer.getName());

        customer.setName("Henzo");
        Assert.assertNotEquals("Murilo", customer.getName());
    }

    @Test
    public void TestOnlyFemaleInArray() {
        Pattern patternFemale = Pattern.compile("-\\s[Female|female]+");

        String[] userSplit = {"Maria - Female", "Cris - Female", "Murilo - Male"};
        List<String> arrayFemale = Arrays.stream(userSplit).filter(patternFemale.asPredicate()).collect(Collectors.toList());

        arrayFemale.forEach(s -> {
            Assert.assertTrue(s.contains("Female"));
            Assert.assertFalse(s.contains("Male"));
        });
    }
}
