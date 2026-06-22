package exercicio;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Demo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Pattern patternFemale = Pattern.compile("-\\s[Ff]+");

        System.out.print("Adicione usurios com nome e sexo, separados por virgula\n ");
        String user = sc.nextLine();
        String[] userSplit = user.split(",");
        List <String> arrayFemale = Arrays.stream(userSplit).filter(patternFemale.asPredicate()).collect(Collectors.toList());
        System.out.println(arrayFemale);
    }
}
