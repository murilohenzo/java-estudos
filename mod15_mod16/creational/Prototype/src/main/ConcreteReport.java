package main;


import main.protos.ReportPrototype;

public class ConcreteReport implements ReportPrototype {

  private String content;

  public ConcreteReport(String content) {
    this.content = content;
  }

  // Construtor de copia para realizar a clonagem
  private ConcreteReport(ConcreteReport source) {
    this.content = source.content;
  }

  @Override
  public ReportPrototype clone() {
    return new ConcreteReport(this);
  }

  @Override
  public void generateReport() {
    System.out.println("Relatorio gerado com conteudo: " + content);
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}