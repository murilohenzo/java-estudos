public class EmailObserver implements Observer {

    @Override
    public void update(Subject subject) {
        System.out.println("Recebendo a noticia via E-mail " + subject.toString());
    }
}
