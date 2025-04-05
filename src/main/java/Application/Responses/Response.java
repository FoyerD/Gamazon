package Application.Responses;

public class Response {
    private String errorMessage;

    public Response(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Response() { }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean errorOccurred() { 
        return errorMessage != null; 
    }
}
