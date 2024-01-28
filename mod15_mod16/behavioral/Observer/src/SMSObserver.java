public class SMSObserver implements Observer {

    @Override
    public void update(Subject subject) {
        System.out.println("Recebendo a noticia via SMS " + subject.toString());
    }
}
