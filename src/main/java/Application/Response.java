package Application;

public class Response<T> {
    private T value;
    private Error errorMessage;

    public Response(T value) {
        this.value = value;
        this.errorMessage = null;
    }

    public Response() {
        this.errorMessage = null;
        this.value = null;
    }

    public Response(Error errorMessage) {
        this.errorMessage = errorMessage;
        this.value = null;
    }


    public String getErrorMessage() {
        return errorMessage.getErrorMessage();
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