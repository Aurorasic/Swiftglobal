package cn.primeledger.cas.global.script;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author yuguojia
 * @create 2018-02-22
 **/
@Getter
@Setter
public class Script {
    /**The program is a set of chunks where each element is either [opcode] or [data, data, data ...]*/
    private List<ScriptChunk> chunks;
}