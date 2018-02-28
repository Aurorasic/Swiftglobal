package cn.primeledger.cas.global.script;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yuguojia
 * @create 2018-02-22
 **/
@Getter
@Setter
public class ScriptChunk {

    /**
     * operation defined in {@link ScriptOpCodes}.
     * */
    private int opcode;

    /**
     * data to be pushed on the stack. It maybe empty or null.
     */
    private byte[] data;
}