package Application;

public class Response<T> {
    private T value;
    private String errorMessage;

    public Response(T value) {
        this.value = value;
    }

    public Response() {
        this.errorMessage = "";
        this.value = null;
    }

    public Response(String errorMessage) {
        this.errorMessage = errorMessage;
        this.value = null;
    }


    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean errorOccurred() { 
        return errorMessage != null; 
    }

    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }
}