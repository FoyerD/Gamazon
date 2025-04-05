package Application.Responses;

public class ResponseT<T> extends Response {
    private T value;

    public ResponseT(T value) {
        super();
        this.value = value;
    }

    public ResponseT() {
        super();
        this.value = null;
    }

    public ResponseT(String errorMessage) {
        super(errorMessage);
    }

    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }
}
