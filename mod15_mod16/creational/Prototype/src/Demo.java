import main.ConcreteReport;

public class Demo {
  
  public static void main(String[] args)  {

    ConcreteReport prototypeReport  = new ConcreteReport("Conteudo do relatorio original");

    ConcreteReport reportCopy1 = (ConcreteReport) prototypeReport.clone();
    ConcreteReport reportCopy2 = (ConcreteReport) prototypeReport.clone();

    reportCopy1.generateReport();
    reportCopy2.generateReport();

    reportCopy1.setContent("Conteudo modificado 1");
    reportCopy2.setContent("Conteudo modificado 2");
    reportCopy1.generateReport();
    reportCopy2.generateReport();
  }

}
