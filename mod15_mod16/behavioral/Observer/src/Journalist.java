import java.util.ArrayList;
import java.util.List;

public class Journalist implements Subject {
    protected static final List<Observer> observers = new ArrayList<>();
    private String title;

    @Override
    public void add(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void remove(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyAll(String title) {
        this.title = title;
        for (Observer observer: observers) {
            observer.update(this);
        }
    }

    @Override
    public String toString() {
        return "title: '" + title + '\'';
    }
}
