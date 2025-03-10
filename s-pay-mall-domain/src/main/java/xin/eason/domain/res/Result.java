package xin.eason.domain.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xin.eason.common.constant.MessageConstant;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private String msg;

    private Integer code;

    private T data;

    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.setMsg(MessageConstant.SUCCESS_MSG);
        result.setCode(1);
        return result;
    }

    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setMsg(MessageConstant.SUCCESS_MSG);
        result.setCode(1);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg){
        Result<T> result = new Result<>();
        result.setMsg(msg);
        result.setCode(0);
        return result;
    }
}
