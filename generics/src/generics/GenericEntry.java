package generics;

public class GenericEntry<T, E> {
    private T data;

    private E code;

    public GenericEntry(T data, E code) {
        this.data = data;
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public E getCode() {
        return code;
    }

    public void setCode(E code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "generics.GenericEntry{" +
                "data=" + data +
                ", code=" + code +
                '}';
    }
}
