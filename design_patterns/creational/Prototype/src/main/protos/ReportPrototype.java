package main.protos;

public interface ReportPrototype {
  public ReportPrototype clone();
  public void generateReport();
}
