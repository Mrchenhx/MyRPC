package model;

/**
 * @Author: Richard
 * @Create: 2021/08/10 21:17:00
 * @Description: 封装了返回给远程调用端的响应数据
 */
public class Response {
    private String requestId;
    private Throwable error;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isError(){
        return error != null;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "requestId='" + requestId + '\'' +
                ", error=" + error +
                ", result=" + result +
                '}';
    }
}
