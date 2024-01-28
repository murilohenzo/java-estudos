package workers;

public abstract class Worker {
    public void routineExecute(){
        routineStart();
        wakeUp();
        goToWork();
        work();
        comeBackHome();
    }
    protected abstract void work();
    private void comeBackHome() {
        System.out.println("Voltando para casa");
    }
    private void goToWork() {
        System.out.println("Indo trabalhar");
    }
    private void routineStart(){
        System.out.println("Iniciando rotina");
    }
    protected void wakeUp() {
        System.out.println("Levantando da cama");
    }
}
