public class TVObserver implements Observer {

    @Override
    public void update(Subject subject) {
        System.out.println("Recebendo a noticia via TV " + subject.toString());
    }
}
